let stompClient = null;

let stompClientEnabled = false;
let IRT3TVAF0001 = false;
let IRO1FOLD0001 = false;

function connect() {
    // const socket = new SockJS('/streamer')
    // stompClient = Stomp.over(socket)

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

            stompClient.subscribe('/user/topic/prices', function (message) {

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

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect()
    }
    console.log("Disconnected")
}

$(function () {
    $('#websocketSwitch').click(function() {
        if ($(this).prop('checked')) {
            connect()
            stompClientEnabled = true;
        } else {
            disconnect()
            stompClientEnabled = false;
        }
    })

    $('#IRT3TVAF0001').click(function() {
        if ($(this).prop('checked')) {
            console.log('IRT3TVAF0001 checked')
            IRT3TVAF0001 = true;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClient.send("/topic/prices/add-isins", {}, message)
            }

        } else {
            console.log('IRT3TVAF0001 unchecked')
            IRT3TVAF0001 = false;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRT3TVAF0001'] });
                stompClient.send("/topic/prices/remove-isins", {}, message)
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
                stompClient.send("/topic/prices/add-isins", {}, message)
            }
        } else {
            console.log('IRO1FOLD0001 checked')

            IRO1FOLD0001 = false;

            if(stompClientEnabled)
            {
                const message = JSON.stringify({ isins: ['IRO1FOLD0001'] });
                stompClient.send("/topic/prices/remove-isins", {}, message)
            }
        }
    })

    $('#chatForm').submit(function(e) {
        e.preventDefault();

        const fromUser = $("#fromUser").val()
        const toUser = $("#toUser").val()
        const $comment = $("#comment")
        const comment = $comment.val()
        const timestamp = new Date()

        if (fromUser.length !== 0 && comment.length !== 0) {
            const chatMessage = JSON.stringify({fromUser, toUser, comment, timestamp})
            stompClient.send("/app/chat", {}, chatMessage)
            $comment.val('')
        }
    })

    let height = window.innerHeight - 245
    $('#priceList').parent().css({"height": height, "max-height": height, "overflow-y": "auto"})

    height = window.innerHeight - 500
    $('#chat').parent().css({"height": height, "max-height": height, "overflow-y": "auto"})

    // connect()
})