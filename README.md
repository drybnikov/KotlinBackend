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
    <li><a href='http://localhost:4567/users'>List of users: /users</a></li>
    <li><a href='http://localhost:4567/login?id=:id'>User login with ID: /login?id=:id</a></li>
    <li><a href='http://localhost:4567/user'>User details:  /user</a></li>
    <li><a href='http://localhost:4567/user/account'>User Account details: /user/account</a></li>
    <li><a href='http://localhost:4567/user/account/deposit?amount=:value'>Deposit: /user/account/deposit?amount=:value</a></li>
    <li><a href='http://localhost:4567/user/account/transfer?to=:userId&amount=:value'>Transfer beetwen accounts: /user/account/transfer?to=:userId&amount=:value</a></li>
</ul>
