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


    </style>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            crossorigin="anonymous"></script>
</head>
<body>

<h1>Настройки</h1>
<div style="width: auto; margin-top: 200px" class="mx-auto p-2 mt-100">
    <h3>Предметные области</h3>
    <div id="context-container">
        <#list context as ctx>
            <div class="context-group">
                <input type="hidden" class="context-code" value="${ctx.code}">
                <label>
                    Наименование:
                    <input type="text" class="context-name" value="${ctx.name}">
                </label>
                <label>
                    Описание:
                    <input type="text" class="context-description" value="${ctx.description}">
                </label>
                <br><br>
            </div>

        </#list>
    </div>

    <template id="new-context-template">
        <div class="context-group">
            <input type="hidden" class="context-code">
            <label>
                Наименование:
                <input type="text" class="context-name">
            </label>
            <label>
                Описание:
                <input type="text" class="context-description">
            </label>

            <button type="button" class="btn btn-danger btn-remove">×</button>
            <br><br>
        </div>
    </template>

    <div class="action-buttons">
        <button type="button" class="btn btn-success" id="context_add_btn">Добавить</button>
        <button type="button" class="btn btn-primary" id="context_save_btn">Сохранить</button>
    </div>

    <#--    --------------------------------------------------------------------------->
    <br><br><br>
    <hr>
    <br><br><br>

    <h3>Поля</h3>
    <div id="fields-container">
        <#list fields as field>
            <div class="field-group">
                <input type="hidden" class="field-code" value="${field.code}">
                <label>
                    Наименование:
                    <input type="text" class="field-name" value="${field.name}">
                </label>
                <label>
                    Описание:
                    <input type="text" class="field-description" value="${field.description}">
                </label>
                <label>
                    Пример запроса:
                    <input type="text" class="field-example-request" value="${field.exampleRequest}">
                </label>
                <label>
                    Пример ответа:
                    <input type="text" class="field-example-response" value="${field.exampleResponse}">
                </label>
                <br><br>
            </div>
        </#list>
    </div>

    <template id="new-field-template">
        <div class="field-group">
            <input type="hidden" class="field-code">
            <label>
                Наименование:
                <input type="text" class="field-name">
            </label>
            <label>
                Описание:
                <input type="text" class="field-description">
            </label>
            <label>
                Пример запроса:
                <input type="text" class="field-example-request">
            </label>
            <label>
                Пример ответа:
                <input type="text" class="field-example-response">
            </label>
            <button type="button" class="btn btn-danger btn-remove">×</button>
            <br><br>
        </div>
    </template>

    <div class="action-buttons">
        <button type="button" class="btn btn-success" id="field_add_btn">Добавить</button>
        <button type="button" class="btn btn-primary" id="field_save_btn">Сохранить</button>
    </div>

    <#--    --------------------------------------------------------------------------->

    <br><br><br>
    <hr>
    <br><br><br>

    <h3>Языковые модели</h3>
    <form method="post" action="/home/settings/model">
        <div class="models">

            <label for="models"></label>
            <select name="model" id="models">
                <#list models as model>
                    <option name="${model}">${model}</option>
                </#list>
            </select>


            <button type="submit" class="btn btn-primary" id="model_save_btn">Сохранить</button>
        </div>
    </form>


</div>
<script>
    document.getElementById('field_add_btn').addEventListener('click', () => {
        const template = document.getElementById('new-field-template');
        const clone = template.content.cloneNode(true);
        document.getElementById('fields-container').appendChild(clone);
    });

    document.getElementById('context_add_btn').addEventListener('click', () => {
        const template = document.getElementById('new-context-template');
        const clone = template.content.cloneNode(true);
        document.getElementById('context-container').appendChild(clone);
    });

    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-remove')) {
            try {
                e.target.closest('.field-group').remove();
            } catch (e) {
            }
            try {
                e.target.closest('.context-group').remove();
            } catch (e) {
            }

        }
    });


    document.getElementById('field_save_btn').addEventListener('click', function () {
        const fieldGroups = document.querySelectorAll('.field-group');
        const fieldsData = [];

        fieldGroups.forEach(group => {
            const code = group.querySelector('.field-code').value;
            const name = group.querySelector('.field-name').value;
            const description = group.querySelector('.field-description').value;
            const exampleRequest = group.querySelector('.field-example-request').value;
            const exampleResponse = group.querySelector('.field-example-response').value;

            fieldsData.push({
                code: code,
                name: name,
                description: description,
                exampleRequest: exampleRequest,
                exampleResponse: exampleResponse
            });
        });

        fetch('/home/settings/fields', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(fieldsData)
        })
            .then(response => {
                if (response.ok) {
                    alert('Данные успешно сохранены!');
                } else {
                    alert('Ошибка при сохранении.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
            });
    });

    document.getElementById('context_save_btn').addEventListener('click', function () {
        const contextGroups = document.querySelectorAll('.context-group');
        const contextData = [];

        contextGroups.forEach(group => {
            const code = group.querySelector('.context-code').value;
            const name = group.querySelector('.context-name').value;
            const description = group.querySelector('.context-description').value;

            contextData.push({
                code: code,
                name: name,
                description: description
            });
        });

        fetch('/home/settings/context', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(contextData)
        })
            .then(response => {
                if (response.ok) {
                    alert('Данные успешно сохранены!');
                } else {
                    alert('Ошибка при сохранении.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
            });
    });
</script>

</body>
</html>
