# Итоговый проект по дисциплине Программирование на языке Java

### Запуск проект
Создание контейнера с БД
```
docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 postgres
```
Создать Jar файл:
- Открыть проект в IntelliJ IDEA (File → Open). 2
- Сделать видимым окно Maven (View → Tool Windows → Maven). 2
- Расширить проект в дереве, расширить «Жизненный цикл» (Lifecycle) и дважды кликнуть по «Пакет» (package). 2
- Maven скомпилирует пакет, и JAR-файл будет записан в директорию target/

Запустить jar файл:
```
java -jar .\target\short_links-1.0-SNAPSHOT-jar-with-dependencies.jar
```
