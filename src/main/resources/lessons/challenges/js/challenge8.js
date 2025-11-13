$(document).ready(function () {
    loadVotes();
    average();
});

function loadVotes() {
    $.get("challenge/8/votes/", function (votes) {
        var totalVotes = 0;

        for (var i = 1; i <= 5; i++) {
            totalVotes = totalVotes + votes[i];
        }

        // Захист від ділення на нуль
        if (totalVotes === 0) {
            for (var j = 1; j <= 5; j++) {
                $('#progressBar' + j).width('0%');
                $("#nrOfVotes" + j).text(0);
            }
            return;
        }

        for (var i = 1; i <= 5; i++) {
            var percent = votes[i] * 100 / totalVotes;
            var progressBar = $('#progressBar' + i);
            progressBar.width(Math.round(percent) * 2 + '%');
            $("#nrOfVotes" + i).text(votes[i]);
        }
    });
}

function average() {
    $.get("challenge/8/votes/average", function (average) {
        var number = average["average"];

        for (var i = 1; i <= 5; i++) {
            var star = $("#star" + i);
            star.removeClass('btn-warning btn-default btn-grey');

            if (i <= number) {
                star.addClass('btn-warning');
            } else {
                star.addClass('btn-grey');
            }
        }
    });
}

function doVote(stars) {
    var $msg = $("#voteResultMsg");

    $msg.hide()
            .removeClass('alert-danger alert-success alert-dismissable');

    $.get("challenge/8/vote/" + encodeURIComponent(stars), function (result) {

        if (result["error"]) {
            $msg.addClass('alert-danger alert-dismissable');
        } else {
            $msg.addClass('alert-success alert-dismissable');
        }

        // 🔐 ФІКС XSS: використовуємо text() замість html()
        $msg.text(result["message"]);
        $msg.show();

        // Оновлюємо лише після успішної відповіді сервера
        loadVotes();
        average();
    });
}
