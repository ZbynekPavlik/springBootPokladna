// Funkce pro načtení dat pomocí AJAX
function loadDataAndCreateChart() {
    // Načtení dat z endpointu /cashflow
    $.ajax({
        url: '/api/transactions/last20',
        type: 'GET',
        success: function(data) {
            // Úspěšně načtená data
            const labels = data.map(entry => entry.idTransaction); // Použijeme saleId jako popisky osy X
            const balances = data.map(entry => entry.balanceAfter); // Použijeme balanceAfter jako data

            // Vytvoření grafu pomocí Chart.js
            var ctx = document.getElementById("AreaChartBalanceBefore");
            var myLineChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: "Cash Flow",
                        lineTension: 0.3,
                        backgroundColor: "rgba(2,117,216,0.2)",
                        borderColor: "rgba(2,117,216,1)",
                        pointRadius: 5,
                        pointBackgroundColor: "rgba(2,117,216,1)",
                        pointBorderColor: "rgba(255,255,255,0.8)",
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: "rgba(2,117,216,1)",
                        pointHitRadius: 50,
                        pointBorderWidth: 2,
                        data: balances,
                    }],
                },
                options: {
                    scales: {
                        xAxes: [{
                            gridLines: {
                                display: false
                            },

                            scaleLabel: { // Popisek osy X
                                                display: true,
                                                labelString: 'ID transankce' // Text popisku
                                            }

                        }],
                        yAxes: [{
                            ticks: {
                                callback: function(value, index, values) {
                                    return value + ' Kč'; // Přidáme Kč k hodnotám na ose Y
                                }
                            },
                            gridLines: {
                                color: "rgba(0, 0, 0, .125)",
                            },

                                            scaleLabel: { // Popisek osy Y
                                                display: true,
                                                labelString: 'Stav po transakci' // Text popisku
                                            }

                        }],

                    },
                    legend: {
                        display: false
                    }
                }
            });
        },
        error: function(xhr, status, error) {
            // Chyba při načítání dat
            console.error(error);
        }
    });
}

