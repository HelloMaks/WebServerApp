package component

import react.FC
import react.Props
import ru.altmanea.webapp.data.Discipline
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Teacher

external interface DataBuildProps : Props {
    var triples: List<Triple<String, String, List<String>>>
    var loadData: (List<Teacher>) -> Unit
}

val CDataBuild = FC<DataBuildProps>("DataBuild") { props ->
    val teachers = props.triples.map { it.first }.distinct() // Коллекция преподавателей
    val disciplines: Map<String, List<String>> = teachers.associateWith { teacher ->
        props.triples.filter { triple -> triple.first == teacher }.map { it.second }.distinct()
    } // Соотношение преподавателей и их дисциплин

    val discipList: MutableList<Discipline> = mutableListOf() // Коллекция объектов класса дисциплин
    val documents: MutableList<Teacher> = mutableListOf() // Коллекция документов для базы данных Mongo

    /* Аккумулирование групп в единый массив */
    teachers.forEach { teacher ->
        discipList.clear() // Очищение коллекции дисциплин для нового преподавателя
        disciplines[teacher]?.forEach { discipline ->
            val finalData: List<ExcelCell> = props.triples.fold(emptyList()) { acc, triple ->
                if(teacher == triple.first && discipline == triple.second) acc + triple.third
                else acc
            }
            /* Сборка объекта класса дисциплин */
            discipList.add(Discipline(discipline, finalData.distinct().map { Group(it) }.toTypedArray()))
        }

        /* Cборка основного документа для базы данных */
        val documentInDB = Teacher(teacher, discipList.map { it }.distinct().toTypedArray())
        documents.add(documentInDB)
    }

    /* Отправка документов в базу данных */
    props.loadData(documents)
}