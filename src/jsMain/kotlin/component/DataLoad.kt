package component

import js.core.get
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.input
import ru.altmanea.webapp.data.Teacher
import web.html.InputType

external interface DataLoadProps : Props {
    var appState: Int
    var setAppState: StateSetter<Int>
    var addMutation: (List<Teacher>) -> Unit
    var deleteMutation: () -> Unit
}

val CDataLoad = FC<DataLoadProps>("DataLoad") { props ->
    var innerFile by useState<String?>(null) // Содержимое файла

    /* Разработка интерфейса для работы с импортом данных */
    if(props.appState == 0) {
        h3 { + "Выберите файл для загрузки на сервер:" }
        input {
            type = InputType.file
            accept = ".csv"
            onChange = { event ->
                event.target.files!![0].text().then { innerFile = it }
            }
        }
    }

    /* Загрузка данных и их обработка в полезные данные */
    innerFile?.let { inner ->
        div {
            if (props.appState == 0) {
                button {
                    +"Загрузка данных"
                    onClick = {
                        props.setAppState(1)
                    }
                }
            }
            if (props.appState == 1) {
                CSVParser {
                    innerCSV = inner // Содержимое CSV-файла
                    loadData = { teachers -> props.addMutation(teachers) }
                }
            }
        }
    }

    /* Работа с полезными данными после обработки */
    if(props.appState == 3) {
        h3 { +"Манипуляция с данными:" }
        div {
            button {
                +"Удаление данных"
                onClick = { props.deleteMutation() }
            }
        }
    }
}