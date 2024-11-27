let stompClient = null;
let stompClientTransaction = null;

let stompClientEnabled = false;
let stompClientEnabledTransaction = false;
let IRT3TVAF0001 = false;
let IRO1FOLD0001 = false;
let IRT3TVAF0001Transaction = false;
let IRO1FOLD0001Transaction = false;


let portfolio_1271 = false;
let portfolio_1270 = false;

let BUY = false;
let SELL = false;

function connectPrice() {

    stompClient = Stomp.client('ws://localhost:8080/streamer');


    let prevPriceValue = null
    stompClient.connect({},
        function (frame) {
            console.log('Connected: ' + frame)

            let isin_list = [];
            if(IRO1FOLD0001)
                isin_list.push('IRO1FOLD0001')
            if(IRT3TVAF0001)
                isin_list.push('IRT3TVAF0001')

            stompClient.subscribe('/user/topic/price', function (message) {

                const prices = JSON.parse(message.body);

                 prices.forEach(function(price){
                     console.log(price);

                     // const priceBody = JSON.parse(price.body)
                     const isin = price.isin
                     const priceValue = price.value
                     const priceTimestamp = price.timestamp

                     if (prevPriceValue == null) {
                         prevPriceValue = priceValue
                     }
                     const priceVar = priceValue - prevPriceValue
                     prevPriceValue = priceValue

                     $('#currentPrice').text(Number(priceValue).toFixed(2))
                     $('#variation').text((priceVar > 0 ? "+" : "") + Number(priceVar).toFixed(2))

                     const row = '<tr><td>'+isin+'</td><td>'+Number(priceValue).toFixed(2)+'</td><td>'+moment(priceTimestamp).format('YYYY-MM-DD HH:mm:ss')+'</td></tr>'
                     if ($('#priceList tr').length > 20) {
                         $('#priceList tr:last').remove()
                     }
                     $('#priceList').find('tbody').prepend(row)
                 });
            },
            {
                isins: JSON.stringify(isin_list)           // Header with the first parameter
            })
        },
        function() {
            console.log('Unable to connect to Websocket!')
            $('#websocketSwitch').prop('checked', false)
        }
     )
}


function connectTransaction() {

    stompClientTransaction = Stomp.client('ws://localhost:8080/streamer');

    stompClientTransaction.connect({},
        function (frame) {
            console.log('Transaction Connected: ' + frame)

            let portfolioId_list = [];
            if(portfolio_1271)
                portfolioId_list.push(1271)
            if(portfolio_1270)
                portfolioId_list.push(1270)

            let type_list = [];
            if(BUY)
                type_list.push('BUY')
            if(SELL)
                type_list.push('SELL')

            let isin_list = [];
            if(IRT3TVAF0001Transaction)
                isin_list.push('IRT3TVAF0001')
            if(IRO1FOLD0001Transaction)
                isin_list.push('IRO1FOLD0001')

            stompClientTransaction.subscribe('/user/topic/transaction', function (message) {

                    const transactions = JSON.parse(message.body);

                    transactions.forEach(function(transaction){
                        console.log(transaction);

                        const portfolioId = transaction.portfolioId
                        const isin = transaction.isin
                        const countValue = transaction.count
                        const price = transaction.price
                        const value = transaction.value
                        const type = transaction.type
                        const transactionId = transaction.id

                        const transactionTimestamp = transaction.timestamp

                        const row = '<tr><td>'+transactionId+'</td><td>'+portfolioId+'</td><td>'+isin+'</td><td>'+type+'</td><td>'+Number(countValue).toFixed(2)+'</td><td>'+Number(price).toFixed(2)+'</td><td>'+Number(value).toFixed(2)+'</td><td>'+moment(transactionTimestamp).format('YYYY-MM-DD HH:mm:ss')+'</td></tr>'
                        if ($('#transactionList tr').length > 20) {
                            $('#transactionList tr:last').remove()
                        }
                        $('#transactionList').find('tbody').prepend(row)
                    });
                },
                {
                    portfolioIds: JSON.stringify(portfolioId_list),           // Header with the first parameter
                    types: JSON.stringify(type_list),
                    isins: JSON.stringify(isin_list)
                })
        },
        function() {
            console.log('Unable to connect to Transaction Websocket!')
            $('#websocketSwitchTransaction').prop('checked', false)
        }
    )
}

function disconnectPrice() {
    if (stompClient !== null) {
        stompClient.disconnect()
    }
    console.log("Disconnected")
}

function disconnectTransaction() {
    if (stompClientTransaction !== null) {
        stompClientTransaction.disconnect()
    }
    console.log("Disconnected Transaction")
}

$(function () {
    $('#websocketSwitch').click(function() {
        if ($(this).prop('checked')) {
            connectPrice()
            stompClientEnabled = true;
        } else {
            disconnectPrice()
            stompClientEnabled = false;
        }
    })

    $('#websocketSwitchTransaction').click(function() {
        if ($(this).prop('checked')) {
            connectTransaction()
            stompClientEnabledTransaction = true;
        } else {
            disconnectTransaction()
            stompClientEnabledTransaction = false;
        }
    })

    $('#IRT3TVAF0001').click(function() {
        if ($(this).prop('checked')) {
            console.log('IRT3TVAF0001 checked')
            IRT3TVAF0001 = true;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClient.send("/user/topic/price/add-items", {}, message)
            }

        } else {
            console.log('IRT3TVAF0001 unchecked')
            IRT3TVAF0001 = false;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClient.send("/user/topic/price/remove-items", {}, message)
            }
        }
    })

    $('#IRO1FOLD0001').click(function() {
        if ($(this).prop('checked')) {
            console.log('IRO1FOLD0001 checked')

            IRO1FOLD0001 = true;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRO1FOLD0001'] });
                stompClient.send("/user/topic/price/add-items", {}, message)
            }
        } else {
            console.log('IRO1FOLD0001 checked')

            IRO1FOLD0001 = false;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRO1FOLD0001'] });
                stompClient.send("/user/topic/price/remove-items", {}, message)
            }
        }
    })


    $('#IRT3TVAF0001Transaction').click(function() {
        if ($(this).prop('checked')) {
            console.log('IRT3TVAF0001 Transaction checked')
            IRT3TVAF0001Transaction = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }

        } else {
            console.log('IRT3TVAF0001 unchecked')
            IRT3TVAF0001Transaction = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })

    $('#IRO1FOLD0001Transaction').click(function() {
        if ($(this).prop('checked')) {
            console.log('IRO1FOLD0001 Transaction checked')

            IRO1FOLD0001Transaction = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ isins: ['IRO1FOLD0001'] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }
        } else {
            console.log('IRO1FOLD0001 Transaction checked')

            IRO1FOLD0001Transaction = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ isins: ['IRO1FOLD0001'] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })


    $('#portfolio_1271').click(function() {
        if ($(this).prop('checked')) {
            console.log('portfolio_1271 Transaction checked')
            portfolio_1271 = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ portfolioIds: [1271] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }

        } else {
            console.log('portfolio_1271 unchecked')
            portfolio_1271 = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({  portfolioIds: [1271] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })

    $('#portfolio_1270').click(function() {
        if ($(this).prop('checked')) {
            console.log('portfolio_1270 Transaction checked')
            portfolio_1270 = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ portfolioIds: [1270] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }

        } else {
            console.log('portfolio_1270 unchecked')
            portfolio_1270 = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({  portfolioIds: [1270] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })


    $('#BUY').click(function() {
        if ($(this).prop('checked')) {
            console.log('BUY Transaction checked')
            BUY = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ types: ['BUY'] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }

        } else {
            console.log('BUY unchecked')
            BUY = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ types: ['BUY'] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })

    $('#SELL').click(function() {
        if ($(this).prop('checked')) {
            console.log('SELL Transaction checked')
            SELL = true;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ types: ['SELL'] });
                stompClientTransaction.send("/user/topic/transaction/add-items", {}, message)
            }

        } else {
            console.log('SELL unchecked')
            SELL = false;

            if(stompClientEnabledTransaction)
            {
                const message = JSON.stringify({ types: ['SELL'] });
                stompClientTransaction.send("/user/topic/transaction/remove-items", {}, message)
            }
        }
    })


    let height = window.innerHeight - 245
    $('#priceList').parent().css({"height": height, "max-height": height, "overflow-y": "auto"})

    $('#transactionList').parent().css({"height": height, "max-height": height, "overflow-y": "auto"})

    // connect()
})