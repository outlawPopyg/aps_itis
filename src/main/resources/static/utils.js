let websocket;
let appServerWebSocket;
let context;
let processor;
let globalStream;

const websocket_uri = 'ws://192.168.84.177:8765';
const applicationServerWebsocketURI = "http://localhost:8081/websocket";
const bufferSize = 4096;
let isRecording = false;
let globalEntities;

function initWebSocket() {
    const websocketAddress = "ws://192.168.84.177:8765";
    // const websocketAddress = document.getElementById('websocketAddress').value;
    // chunk_length_seconds = document.getElementById('chunk_length_seconds').value;
    chunk_length_seconds = 5
    chunk_offset_seconds = 1;
    // chunk_offset_seconds = document.getElementById('chunk_offset_seconds').value;
    language = "russian";


    if (!websocketAddress) {
        console.log("WebSocket address is required.");
        return;
    }

    websocket = new WebSocket(websocketAddress);

    const sock = new SockJS(applicationServerWebsocketURI);
    const stomp = Stomp.over(sock);

    stomp.connect({}, function (frame) {
        console.log("Connected from the client: " + frame);
        stomp.subscribe("/user/topic/voice-ai", function (message) {
            const data = JSON.parse(message.body);
            const fio = document.getElementById("fio");
            const address = document.getElementById("address");
            fio.textContent = data['fio'];
            address.textContent = data['address'];
        })
    })


    websocket.onopen = () => {
        console.log("WebSocket connection established");
        document.getElementById('startButton').disabled = false;
    };

    websocket.onclose = event => {
        console.log("WebSocket connection closed", event);
        stomp.disconnect();
        document.getElementById('startButton').disabled = true;
        document.getElementById('stopButton').disabled = true;
    };

    websocket.onmessage = event => {
        const transcript_data = JSON.parse(event.data);
        stomp.send("/user/topic/voice-ai/transcribed", {}, JSON.stringify(transcript_data));
        updateTranscription(transcript_data);
    };
}

function updateTranscription(transcript_data) {
    const transcriptionDiv = document.getElementById('transcription');
    const languageDiv = document.getElementById('detected_language');

    if (transcript_data['words'] && transcript_data['words'].length > 0 && Array.isArray(transcript_data['words'])) {
        transcript_data['words'].forEach(wordData => {
            const span = document.createElement('span');
            const probability = wordData['probability'];
            span.textContent = wordData['word'] + ' ';

            if (probability > 0.9) {
                span.style.color = 'green';
            } else if (probability > 0.6) {
                span.style.color = 'orange';
            } else {
                span.style.color = 'red';
            }

            transcriptionDiv.appendChild(span);
        });

        transcriptionDiv.appendChild(document.createElement('br'));
    } else {
        transcriptionDiv.textContent += transcript_data['text'] + '\n';
    }

    fetch(`http://192.168.84.177:8000/extract`, {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({text: transcriptionDiv.textContent, entities: globalEntities})
    }).then(res => res.json())
        .then(entities => {

            entities.forEach(e => {
                let combobox = document.querySelector(`select[id="${e.name}"]`);
                const options = Array.from(combobox.options).map(o => o.text);

                if (e.value !== undefined && e.value !== '' && options.every(o => o !== e.value)) {
                    console.log(e.value)
                    combobox.add(new Option(e.value, e.name, true, false));
                }
            });

        })

}


function startRecording(entities) {
    globalEntities = entities;
    if (isRecording) return;
    isRecording = true;

    const AudioContext = window.AudioContext || window.webkitAudioContext;
    context = new AudioContext();

    navigator.mediaDevices.getUserMedia({audio: true}).then(stream => {
        globalStream = stream;
        const input = context.createMediaStreamSource(stream);
        processor = context.createScriptProcessor(bufferSize, 1, 1);
        processor.onaudioprocess = e => processAudio(e);
        input.connect(processor);
        processor.connect(context.destination);

        sendAudioConfig();
    }).catch(error => console.error('Error accessing microphone', error));

    document.getElementById('startButton').disabled = true;
    document.getElementById('stopButton').disabled = false;
}

function stopRecording(prompt, serializedFields) {

    let fields = serializedFields.split(",");
    let newPrompt = prompt.replace("{TEXT}", document.getElementById("transcription").textContent)
    newPrompt = newPrompt.replace("{EXAMPLES}", "");
    fetch(`/home/gpt`, {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: newPrompt
    }).then(res => res.json())
        .then(res => {
            fields.forEach(e => {

                let combobox = document.querySelector(`select[id="${e}"]`);
                const options = Array.from(combobox.options).map(o => o.text);

                if (Array.isArray(res[e])) {
                    res[e].forEach(r => {
                        if (r.value !== undefined && r.value !== '' && options.every(o => o !== r)) {
                            console.log(r.value)
                            combobox.add(new Option(res[e], e, true, false));
                        }
                    })
                } else {
                    if (res[e] !== undefined && res[e] !== '' && options.every(o => o !== res[e])) {
                        console.log(res[e])
                        combobox.add(new Option(res[e], e, true, false));
                    }
                }


            })
        }).finally(() => {

        let allOpts = Array.from(document.querySelectorAll(`option`)).map(o => ({
            name: o.value,
            value: capitalizeWords(o.text)
        }));


        fetch("http://192.168.84.177:8001/link_entities", {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(allOpts)
        }).then(res => res.json())
            .then(res => {
                let groupBy = Object.groupBy(res, ({label}) => label);

                let sorted = allOpts.sort((a, b) => {
                    let number = getItems(b.value, groupBy).length - getItems(a.value, groupBy).length;
                    console.log(number)
                    return number;
                });

                document.querySelectorAll('select').forEach(s => s.innerHTML = '');
                sorted.forEach(s => {
                    let combobox = document.querySelector(`select[id="${s.name}"]`);
                    combobox.add(new Option(s.value, s.name, true, false));
                })

            })

        globalEntities = [];
    });

    if (!isRecording) return;
    isRecording = false;

    if (globalStream) {
        globalStream.getTracks().forEach(track => track.stop());
    }
    if (processor) {
        processor.disconnect();
        processor = null;
    }
    if (context) {
        context.close().then(() => context = null);
    }
    document.getElementById('startButton').disabled = false;
    document.getElementById('stopButton').disabled = true;

}

function getItems(key, partial) {
    let items = [];
    for (const [label, items] of Object.entries(partial)) {
        if (label === key) {
            return items;
        }
    }

    return items;
}

function capitalizeWords(str) {
    return str
        .toLowerCase()
        .split(/\s+/)
        .map(word => word ? word[0].toUpperCase() + word.slice(1) : '')
        .join(' ');
}

function sendAudioConfig() {
    let selectedStrategy = document.getElementById('bufferingStrategySelect').value;
    let processingArgs = {};

    if (selectedStrategy === 'silence_at_end_of_chunk') {
        processingArgs = {
            chunk_length_seconds: parseFloat(document.getElementById('chunk_length_seconds').value),
            chunk_offset_seconds: parseFloat(document.getElementById('chunk_offset_seconds').value)
        };
    }

    const audioConfig = {
        type: 'config',
        data: {
            sampleRate: context.sampleRate,
            bufferSize: bufferSize,
            channels: 1, // Assuming mono channel
            language: language,
            processing_strategy: selectedStrategy,
            processing_args: processingArgs
        }
    };

    websocket.send(JSON.stringify(audioConfig));
}

function downsampleBuffer(buffer, inputSampleRate, outputSampleRate) {
    if (inputSampleRate === outputSampleRate) {
        return buffer;
    }
    var sampleRateRatio = inputSampleRate / outputSampleRate;
    var newLength = Math.round(buffer.length / sampleRateRatio);
    var result = new Float32Array(newLength);
    var offsetResult = 0;
    var offsetBuffer = 0;
    while (offsetResult < result.length) {
        var nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
        var accum = 0, count = 0;
        for (var i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++) {
            accum += buffer[i];
            count++;
        }
        result[offsetResult] = accum / count;
        offsetResult++;
        offsetBuffer = nextOffsetBuffer;
    }
    return result;
}

function processAudio(e) {
    const inputSampleRate = context.sampleRate;
    const outputSampleRate = 16000; // Target sample rate

    const left = e.inputBuffer.getChannelData(0);
    const downsampledBuffer = downsampleBuffer(left, inputSampleRate, outputSampleRate);
    const audioData = convertFloat32ToInt16(downsampledBuffer);

    if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.send(audioData);
    }
}

function convertFloat32ToInt16(buffer) {
    let l = buffer.length;
    const buf = new Int16Array(l);
    while (l--) {
        buf[l] = Math.min(1, buffer[l]) * 0x7FFF;
    }
    return buf.buffer;
}

function toggleBufferingStrategyPanel() {
    var selectedStrategy = document.getElementById('bufferingStrategySelect').value;
    if (selectedStrategy === 'silence_at_end_of_chunk') {
        var panel = document.getElementById('silence_at_end_of_chunk_options_panel');
        panel.classList.remove('hidden');
    } else {
        var panel = document.getElementById('silence_at_end_of_chunk_options_panel');
        panel.classList.add('hidden');
    }
}

