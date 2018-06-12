if (window.console) {
  console.log("Welcome to your Play application's JavaScript!");
}


$(document).ready(function() {
    var table = $('#carstable').DataTable();

    var mylabels = table.column( 0, {order:'current'} ).data();

    var myDataCoutAnnelNet = table.column( 6, {order:'current'} ).data();

    var myDataATNannuel = table.column( 7, {order:'current'} ).data().map(x => x*12);

    var myDataGainIsoc = table.column( 5, {order:'current'} ).data();


    var ctx = $("#myChart");
    var myChart = new Chart(ctx, {
        type: 'bar',
        responsive: true,
        data: {
            labels: mylabels,
            datasets: [{
                label: 'gain ISOC (euros)',
                data: myDataGainIsoc,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(255, 206, 86, 1)',
                borderWidth: 1
                },
                {
                label: 'Coût NET Total (euros)',
                data: myDataCoutAnnelNet,
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255,99,132,1)',
                borderWidth: 1
            },
            {
                label: 'ATN annuel (euros)',
                data: myDataATNannuel,
                backgroundColor: 'rgba(255, 206, 86, 0.2)',
                borderColor: 'rgba(255, 206, 86, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero:true
                    }
                }]
            }
        }
    });

} );

