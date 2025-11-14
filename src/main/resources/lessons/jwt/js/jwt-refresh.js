$(document).ready(function () {
    // Автологін із захардкодженим паролем прибираємо.
    // Логін тепер має викликатися з форми:
    // login($('#username').val(), $('#password').val());
});

function login(user, password) {
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({
            user: user,
            password: password          //  пароль більше не захардкоджений
        })
    }).success(function (response) {
        localStorage.setItem('access_token', response['access_token']);
        localStorage.setItem('refresh_token', response['refresh_token']);
    });
}

// Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs = webgoat.customjs || {};
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    var accessToken = localStorage.getItem('access_token');
    if (accessToken) {
        headers_to_set['Authorization'] = 'Bearer ' + accessToken;
    }
    return headers_to_set;
};

// Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
function newToken() {
    var refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
        return;
    }

    $.ajax({
        headers: webgoat.customjs.addBearerToken(),
        type: 'POST',
        url: 'JWT/refresh/newToken',
        contentType: "application/json",
        data: JSON.stringify({ refreshToken: refreshToken })
    }).success(function (response) {
        //  оновлюємо токени з відповіді сервера, а не з неіснуючих змінних
        localStorage.setItem('access_token', response['access_token']);
        localStorage.setItem('refresh_token', response['refresh_token']);
    });
}
