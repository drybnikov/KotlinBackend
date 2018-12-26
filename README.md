[![Build Status](https://travis-ci.org/drybnikov/kotlinbackend.svg)](https://travis-ci.org/drybnikov/kotlinbackend) [![Codecov](https://codecov.io/github/drybnikov/kotlinbackend/coverage.svg)](https://codecov.io/gh/drybnikov/kotlinbackend)

# Kotlin Backend
Simple RESTful API on Kotlin (including data model and the backing implementation) for money transfers between accounts.

Test API:
<ul>
    <li><a href='http://localhost:4567/users'>List of users: /users</a></li>
    <li><a href='http://localhost:4567/login?id=:id'>User login with ID: /login?id=:id</a></li>
    <li><a href='http://localhost:4567/user'>User details:  /user</a></li>
    <li><a href='http://localhost:4567/user/account'>User Account details: /user/account</a></li>
    <li><a href='http://localhost:4567/user/account/deposit?amount=:value'>Deposit: /user/account/deposit?amount=:value</a></li>
    <li><a href='http://localhost:4567/user/account/transfer?to=:userId&amount=:value'>Transfer beetwen accounts: /user/account/transfer?to=:userId&amount=:value</a></li>
</ul>
