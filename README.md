# SQL (AQA_Exercise_3.2-1)
## Домашнее задание по курсу "Автоматизированное тестирование"
## Тема: «3.2. SQL», задание №1: «Скоро deadline»
1. Создание Docker Container на базе MySQL 8:
	- прописано создание БД, пользователя, пароля
	- прописано использование приложенной схемы БД (schema.sql)
1. Запуск SUT (app-deadline.jar) через соотвествующие флаги
1. Протестирован "Вход в систему" через веб-интерфейс:
	- используется обращение к БД для получения сгенерированного кода
### Предварительные требования
1. Получить доступ к удаленному серверу
1. На удаленном сервере должны быть установлены и доступны:
	- GIT
	- Docker	
	- Bash
1. На компьютере пользователя должна быть установлена:
	- Intellij IDEA
### Установка и запуск
1. Залогиниться на удаленный сервер
1. Склонировать проект на удаленный сервер командой
	```
	git clone https://github.com/Lognestix/AQA_Exercise_3.2-1
	```
1. Перейти в созданный каталог командой
	```
	cd AQA_Exercise_3.2-1
	```
1. Создать и запустить Docker Container на базе MySQL 8 командой
	```
	docker-compose up
	```
1. Склонировать проект на свой компьютер
	- открыть терминал
	- ввести команду 
		```
		git clone https://github.com/Lognestix/AQA_Exercise_3.2-1
		```
1. Открыть склонированный проект в Intellij IDEA
1. В Intellij IDEA перейти во вкладку Terminal (Alt+F12) и запустить SUT командой
	```
	java -jar artifacts/app-deadline.jar -P:jdbc.url=jdbc:mysql://185.119.57.164:3306/base -P:jdbc.user=adm -P:jdbc.password=9mRE
	```
1. Запустить авто-тесты (Shift+F10)