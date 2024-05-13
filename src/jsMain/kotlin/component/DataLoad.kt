package component

import js.core.get
import react.*
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import ru.altmanea.webapp.data.Teacher
import web.buffer.Blob
import web.file.FileReader
import web.html.InputType

external interface DataLoadProps : Props {
    var addMutation: (List<Teacher>) -> Unit
}

val CDataLoad = FC<DataLoadProps>("DataLoad") { props ->
    var blob: Blob? by useState(null) // Выбранный файл
    var innerFile: String? by useState(null) // Содержимое выбранного файла

    val encs = arrayOf("UTF-8", "CP1251", "CP866", "KOI-8R", "ISO-8859-5") // Массив кодировок
    var selEnc by useState(encs[0]) // Выбранная кодировка

    /* Разработка интерфейса для работы с импортом данных */
    h3 { + "Выберите файл для загрузки:" }
    div {
        b { + "Кодировка: " }
        select {
            value = selEnc
            encs.forEach { enc -> option { + enc } }
            onChange = { event -> selEnc = event.target.value }
        }
    }
    div {
        b { + "Файл для импорта: " }
        input {
            type = InputType.file
            accept = ".csv"
            onChange = { event -> blob = event.target.files!![0] }
        }
    }

    /* Загрузка данных и их обработка в полезные данные */
    blob?.let { file ->
        div {
            button {
                b { + "Загрузка данных" }
                onClick = {
                    FileReader().apply { readAsText(file, selEnc) }
                        .onload = { innerFile = it.target.asDynamic().result as? String }
                }
            }
            innerFile?.let { inner ->
                CSVParser {
                    innerCSV = inner // Содержимое CSV-файла
                    setInnerFile = { value -> innerFile = value }
                    loadData = { teachers -> props.addMutation(teachers) }
                }
            }
        }
    }
}