import stomp from 'k6/x/stomp';
import http from 'k6/http';
import {check} from 'k6';
import {Trend} from 'k6/metrics';

const conn2cons = new Trend('conn2cons');
const e2e = new Trend('e2e');

export const options = {
    vus: 1, // number of virtual users
    duration: '1000s', // test duration
};

function ws()
{
    const connAt = Date.now(); // Unix timestamp in milliseconds
    console.log('connAt: ' + connAt);

    const client = stomp.connect({
        protocol: 'ws',
        addr: 'localhost:9900',
        timeout: '150s',
        tls: false,
        path: '/streamer/ws',
        heartbeat: {
            incoming: '30s',
            outgoing: '30s',
        },
        message_send_timeout: '500s',
        receipt_timeout: '10s',
        read_buffer_size: 4096,
        read_channel_capacity: 20,
        write_buffer_size: 4096,
        write_channel_capacity: 20,
        verbose: false,
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5OCIsImlhdCI6MTczMjYyMzA4NCwiZXhwIjoxNzMzNDg3MDg0LCJyb2xlIjoiQURNSU4iLCJ0b2tlbklkIjoiMTMxMDk0MDk3NTQyMTMyOTQwOCIsIm1hbmFnZXJOYW1lIjoi2LHYrdmF2KfZhiDYrNmH2K_Yotiz2KciLCJlbWFpbCI6IlIuSmFoZGFzYUBlbW9maWQuY29tIn0.TIvYl6DXhest7c__7OdT5BLAiLR6fQixucr0t-C81B3BL8g6Vm3VCdX7OyxjjJCh4SoPUBlGmqM8XvxHM9jL9Q',
        }
    });

     // subscribe to receive messages from '/topic/greetings' with auto ack
    let subscription = client.subscribe('/user/topic/quote',
        {
            headers: {
                'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5OCIsImlhdCI6MTczMjYyMzA4NCwiZXhwIjoxNzMzNDg3MDg0LCJyb2xlIjoiQURNSU4iLCJ0b2tlbklkIjoiMTMxMDk0MDk3NTQyMTMyOTQwOCIsIm1hbmFnZXJOYW1lIjoi2LHYrdmF2KfZhiDYrNmH2K_Yotiz2KciLCJlbWFpbCI6IlIuSmFoZGFzYUBlbW9maWQuY29tIn0.TIvYl6DXhest7c__7OdT5BLAiLR6fQixucr0t-C81B3BL8g6Vm3VCdX7OyxjjJCh4SoPUBlGmqM8XvxHM9jL9Q',
            }
        });


    // send a message to '/app/hello' with application/json as MIME content-type
    let payload = {"isins":["IRT1ATML0001",
            "IRT1ARAS0001",
            "IRB6AF250631",
            "IRS4AMIN0011",
            "IRO3Z0870001",
            "IRB4O06605C1",
            "IRB4O0650531",
            "IRB4O0640431",
            "IRT3NRJF0001",
            "IRT1BIDF0001",
            "IRB3NZ010641",
            "IRB3NZ020641",
            "IROAMOJF0571",
            "IROAMOJF0521",
            "IROAMOJF0541",
            "IROAMOJF0491",
            "IRO1FRIS0001",
            "IRO1FRVR0001",
            "IRO1FTIR0001",
            "IRO1GBEH0001",
            "IRO1GCOZ0001",
            "IRO1GESF0001",
            "IRO1GDIR0001",
            "IRO1GGAZ0001",
            "IRO1GHAT0001",
            "IRO1GHEG0001",
            "IRO1GHND0001",
            "IRO1GLOR0001",
            "IRO1GMEL0001",
            "IRO1GMRO0001",
            "IRO1GNBO0001",
            "IRO1GOLG0001",
            "IRO1GORJ0001",
            "IRO1GOST0001",
            "IRO1GPSH0001",
            "IRO1GSBE0001",
            "IRO1GSKE0001",
            "IRO1GTSH0001",
            "IRO1HFRS0001",
            "IRO1HJPT0001",
            "IRO1HMRZ0001",
            "IRO1HSHM0001",
            "IRT1ATML0001",
            "IRT1ARAS0001",
            "IRB6AF250631",
            "IRS4AMIN0011",
            "IRO3Z0870001",
            "IRB4O06605C1",
            "IRB4O0650531",
            "IRB4O0640431",
            "IRT3NRJF0001",
            "IRT1BIDF0001",
            "IRB3NZ010641",
            "IRB3NZ020641",
            "IROAMOJF0571",
            "IROAMOJF0521",
            "IROAMOJF0541",
            "IROAMOJF0491",
            "IRO1FRIS0001",
            "IRO1FRVR0001",
            "IRO1FTIR0001",
            "IRO1GBEH0001",
            "IRO1GCOZ0001",
            "IRO1GESF0001",
            "IRO1GDIR0001",
            "IRO1GGAZ0001",
            "IRO1GHAT0001",
            "IRO1GHEG0001",
            "IRO1GHND0001",
            "IRO1GLOR0001",
            "IRO1GMEL0001",
            "IRO1GMRO0001",
            "IRO1GNBO0001",
            "IRO1GOLG0001",
            "IRO1GORJ0001",
            "IRO1GOST0001",
            "IRO1GPSH0001",
            "IRO1GSBE0001",
            "IRO1GSKE0001",
            "IRO1GTSH0001",
            "IRO1HFRS0001",
            "IRO1HJPT0001",
            "IRO1HMRZ0001",
            "IRO1HSHM0001",
            "IRT3TVAF0001"]};
    client.send('/user/topic/quote/add-filter', 'application/json', JSON.stringify(payload),
    {
        headers: {
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5OCIsImlhdCI6MTczMjYyMzA4NCwiZXhwIjoxNzMzNDg3MDg0LCJyb2xlIjoiQURNSU4iLCJ0b2tlbklkIjoiMTMxMDk0MDk3NTQyMTMyOTQwOCIsIm1hbmFnZXJOYW1lIjoi2LHYrdmF2KfZhiDYrNmH2K_Yotiz2KciLCJlbWFpbCI6IlIuSmFoZGFzYUBlbW9maWQuY29tIn0.TIvYl6DXhest7c__7OdT5BLAiLR6fQixucr0t-C81B3BL8g6Vm3VCdX7OyxjjJCh4SoPUBlGmqM8XvxHM9jL9Q',
        }
    });
    console.log('send /user/topic/quote/add-filter');

    // read the message
    for (let j = 0; j < 1000; j++) {
        // show the message
        const msg = subscription.read();
        // console.log('msg: ' + JSON.stringify(msg));
        console.log('msg count: '+ j);
    }

    conn2cons.add(Date.now() - connAt); // Unix timestamp in milliseconds

    // disconnect from websocket
    client.disconnect();
}

export default function () {
    ws();
}