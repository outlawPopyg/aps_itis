<!DOCTYPE html>

<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Audio Stream to WebSocket Server</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: #f4f4f4;
            text-align: center;
        }

        h1 {
            color: #333;
        }

        .controls {
            margin: 20px auto;
            padding: 10px;
            width: 80%;
            display: flex;
            justify-content: space-around;
            align-items: center;
        }

        .control-group {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .controls input, .controls button, .controls select {
            padding: 8px;
            margin: 5px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 0.9em;
        }

        #transcription {
            margin: 20px auto;
            border: 1px solid #ddd;
            padding: 10px;
            width: 80%;
            height: 150px;
            overflow-y: auto;
            background: white;
        }

        .label {
            font-size: 0.9em;
            color: #555;
            margin-bottom: 5px;
        }

        button {
            cursor: pointer;
        }

        .buffering-strategy-panel {
            margin-top: 10px;
        }

        /* ... existing styles ... */
        .hidden {
            display: none;
        }
    </style>

    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="../stomp.js"></script>
    <script src='../utils.js'></script>

</head>

<body>

<div>
    <div class="controls">
        <button onclick="initWebSocket()">Connect</button>
    </div>

    <button id="startButton" onclick='startRecording()' disabled>Начать разговор</button>
    <button id="stopButton"  onclick="stopRecording(`${prompt}`, `${serializedFields}`)" disabled>Закончить разговор</button>
    <div id="transcription"></div>
    <br/>

    <#list fields as field>
        <div>${field.description}: <b id="${field.name}"></b></div>
        <br />
    </#list>

</div>




</body>
</html>
