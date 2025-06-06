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

        .fields {
            display: flex;

            flex-direction: column;
            align-items: center;
        }

        .buttons {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
    </style>

    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="../stomp.js"></script>
    <script src='../utils.js'></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.2/html2pdf.bundle.min.js"></script>

</head>

<body>

<script>
    let list = [];
    <#list fields as field>
    list.push({name: "${field.name}", description: "${field.description}"})
    </#list>
</script>
<div>
    <div class="controls">
        <button onclick="initWebSocket()">Connect</button>
    </div>

    <div class="buttons">
        <button id="startButton" onclick='startRecording(list)' disabled>Начать разговор</button>
        <button id="stopButton" onclick='stopRecording(`${prompt}`, `${serializedFields}`)' disabled>Закончить разговор
        </button>
    </div>

    <div id="transcription"></div>
    <br/>

    <div class="fields">
        <#list fields as field>
            <label for="${field.name}">${field.description}: </label>
            <select name="combobox" id="${field.name}">

            </select>

            <label for="${field.name}-correction">Исправления: </label>
            <select name="correction" id="${field.name}-correction">

            </select>

            <br><br><br>
        </#list>
    </div>

    <div>
        <button id="correct" class="btn btn-primary">Исправить</button>
    </div>

    <div id="report" class="hidden">
        <label for="formatSelect">Выберите формат:</label>
        <select id="formatSelect">
            <option value="pdf">PDF</option>
            <option value="html">HTML</option>
        </select>
        <button onclick="downloadReport()">Скачать отчёт</button>
    </div>

    <div id="reportContainer"></div>


</div>


</body>
</html>
