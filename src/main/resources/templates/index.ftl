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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</head>
<body>


<div style="width: 500px; margin-top: 200px" class="mx-auto p-2 mt-100" >

    <form method="post" action="/home">
        <div id="context">
            <select name="context" class="form-select form-select-lg mb-3 mx-auto p-2" aria-label="Large select example">
                <option selected>Выберите контекст разговора</option>
                <#list context.children as child>
                    <option value=${child.code}>${child.name}</option>
                </#list>
            </select>

            <div class="mx-auto p-2">
                <button id="context-btn" type="button" class="btn btn-primary">Далее</button>
            </div>
        </div>

        <div id="fields" style="display: none">

            <#list fields.children as field>
                <div class="form-check">
                    <input name="fields" class="form-check-input" type="checkbox" value="${field.code}" id="flexCheckDefault">
                    <label class="form-check-label" for="flexCheckDefault">
                        ${field.description}
                    </label>
                </div>
            </#list>



            <div class="mx-auto p-2">
                <button id="fields-btn" type="submit" class="btn btn-primary">Далее</button>
            </div>
        </div>

    </form>



</div>

<script>
    document.getElementById("context-btn").addEventListener("click", function (e) {
        e.preventDefault();
        document.getElementById("context").style.display = 'none';
        document.getElementById("fields").style.display = 'block';
    })

</script>

</body>
</html>
