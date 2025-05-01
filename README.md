1. `BaseController::home`. Заполняем поля
2. `BaseController::post`. Формируем запрос
3. `utils.js::startRecording`. Начинаем запись
4.  тут идет запрос к удаленному серверу, где идет сегментация по тишине 
5. `utils.js::updateTranscription`. Обрабатываем транскрипцию
6. `main.py::extract_entities`. Обращаемся к первой LLM, обрабатывающая текст в потоке речи
7. `utils.js::stopRecording`. Завершаем разговор
8. `BaseController::gptRequest`. Запрос ко второй LLM, использующая контекст разговора
9. `entity_linking.py::link_entities_combined`. SPARQL запрос к внешним БД.