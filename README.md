[![Build Status](https://travis-ci.org/drybnikov/KotlinBackend.svg)](https://travis-ci.org/drybnikov/KotlinBackend) [![Codecov](https://codecov.io/github/drybnikov/KotlinBackend/coverage.svg)](https://codecov.io/gh/drybnikov/KotlinBackend)

# Kotlin Backend
Simple RESTful API on Kotlin (including data model and the backing implementation) for money transfers between accounts.

# Libraries
* [Spark-kotlin](https://github.com/tipsy/spark-kotlin)
* [Dagger 2](https://github.com/google/dagger)
* [gson](https://github.com/google/gson)
* [h2database](http://h2database.com/html/main.html)
* [jetbrains.exposed](https://github.com/JetBrains/Exposed)
* [junit.jupiter](https://junit.org/junit5/docs/current/user-guide/)
* [mockitokotlin2](https://github.com/nhaarman/mockito-kotlin/)

# Test API:
<ul>
    <li>List of users: <a href='http://localhost:4567/users'>/users</a></li>
    <li>User login with ID: <a href='http://localhost:4567/login?id=:id'>/login?id=:id</a></li>
    <li>User details:  <a href='http://localhost:4567/user'>/user</a></li>
    <li>User Account details: <a href='http://localhost:4567/user/account'>/user/account</a></li>
    <li>Deposit: <a href='http://localhost:4567/user/account/deposit?amount=:value'>/user/account/deposit?amount=:value</a></li>
    <li>Transfer beetwen accounts: <a href='http://localhost:4567/user/account/transfer?to=:userId&amount=:value'>/user/account/transfer?to=:userId&amount=:value</a></li>
</ul>
