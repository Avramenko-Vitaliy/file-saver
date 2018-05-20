## File saver
Что бы развернуть ноды в `file-saver/deploy` необходимо выполнить файл `update_run.sh`.
После чего будут стянуты и подняты три ноды, nginx и Redis.

#### Описание
Redis использован как альтернатива вместо стандартных сетевых протоколов.
Таким образом нода которая более свободна первее вычитает приходящие сообщение.
Библиотека `ua.ardas.redis-client`, используеться компанией Ardas Group Inc.

Для обращение к нодам необходимо указывать к какой именно идет обращение.

Пример:

`http://localhost/node-1/file-api/upload`

`http://localhost/node-1/file-api/33520c2d-645a-4d48-b376-34df43af06a6/download`

#### Методы
`/file-api/upload`

`/file-api/{key:UUID}/download`
