package component

import react.FC
import react.Props
import ru.altmanea.webapp.data.Discipline
import ru.altmanea.webapp.data.Group
import ru.altmanea.webapp.data.Teacher

external interface DataBuildProps : Props {
    var teachHead: Header
    var discipHead: Header
    var groupHead: Header
    var dataList: List<Map<Header, List<DataCell>>>
    var loadData: (List<Teacher>) -> Unit
}

typealias NamePieces = List<String> // Массив из имен, фамилий и отчеств преподавателей

val CDataBuild = FC<DataBuildProps>("DataBuild") { props ->
    val teachers: List<NamePieces> =
        props.dataList.map { it[props.teachHead]!! }.distinct() // Поиск всех преподавателей

    val disciplines: Map<NamePieces, List<String>> = teachers.associateWith { teacher ->
        props.dataList.filter { map ->
            map[props.teachHead]!! == teacher
        }.map { it[props.discipHead]!!.first() }.distinct()
    } // Соотношение преподавателей и их дисциплин

    val discipList: MutableList<Discipline> = mutableListOf() // Коллекция объектов класса дисциплин
    val documents: MutableList<Teacher> = mutableListOf() // Коллекция документов для базы данных Mongo

    /* Аккумулирование групп в единый массив */
    teachers.forEach { teacher ->
        discipList.clear() // Очищение коллекции дисциплин для нового преподавателя
        disciplines[teacher]?.forEach { discipline ->
            val finalMap: Map<Header, List<DataCell>> = props.dataList.fold(
                mapOf(props.teachHead to teacher,
                    props.discipHead to listOf(discipline),
                    props.groupHead to emptyList())) { acc, map ->
                if(acc[props.teachHead] == map[props.teachHead] &&
                    acc[props.discipHead] == map[props.discipHead]) {

                    mapOf(props.teachHead to teacher,
                        props.discipHead to listOf(discipline),
                        props.groupHead to (acc[props.groupHead]!! +
                            map[props.groupHead]!!).distinct()) // Процесс суммирования групп в один массив

                } // Проверка на соответствие данных в заголовках преподавателя и дисциплин
                else acc
            }
            /* Сборка объекта класса дисциплин */
            discipList.add(
                Discipline(
                    name = discipline,
                    groups = finalMap[props.groupHead]!!.distinct().map { Group(it) } .toTypedArray()
                )
            )
        }
        /* Cборка основного документа для базы данных */
        val documentInDB = Teacher(
            firstname = teacher[1],
            surname = teacher[0],
            patronymic = if(teacher.size == 3) teacher[2] else null,
            disciplines = discipList.map { it }.distinct().toTypedArray()
        )
        documents.add(documentInDB)
    }
    /* Отправка документов в базу данных */
    props.loadData(documents)
}