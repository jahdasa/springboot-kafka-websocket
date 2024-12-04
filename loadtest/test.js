import stomp from 'k6/x/stomp';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const conn2cons = new Trend('conn2cons');
const e2e = new Trend('e2e');

export const options = {
    vus: 1, // number of virtual users
    duration: '10s', // test duration
};

function login () {
    const url = 'http://localhost:8080/login';

    const headers = {
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
        'Accept-Language': 'en-US,en;q=0.9',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Cookie': 'Idea-3695921=a51a0e05-54ee-445b-9f11-5d207c84053c; cookiesession1=678ADA77616861A4D180E8B7164FB383; JSESSIONID=A64C7DA4FA331C95F4760717510953B2',
        'Origin': 'http://localhost:8080',
        'Pragma': 'no-cache',
        'Referer': 'http://localhost:8080/login',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'same-origin',
        'Sec-Fetch-User': '?1',
        'Upgrade-Insecure-Requests': '1',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
        'sec-ch-ua': '"Google Chrome";v="131", "Chromium";v="131", "Not_A Brand";v="24"',
        'sec-ch-ua-mobile': '?0',
        'sec-ch-ua-platform': '"Windows"',
    };

    const payload = 'username=user1&password=123';

    const res = http.post(url, payload, { headers, redirects: 0 });

    // console.log(res)
    // Add checks for response status or body
    check(res, {
        'is status 302': (r) => r.status === 302,
        'login is successful': (r) =>
        {
            const locationHeader = r.headers['Location']; // Get the Location header
            return locationHeader && locationHeader.includes('localhost'); // Check if it contains '?continue'
        }
        , // Adjust this based on your app's response
    });

    // Simulate some sleep between requests
    // sleep(1);
}



function ws()
{
    const connAt = Date.now(); // Unix timestamp in milliseconds
    console.log('connAt: ' + connAt);

    const client = stomp.connect({
        protocol: 'ws',
        addr: 'localhost:8080',
        timeout: '15s',
        tls: false,
        path: '/streamer',
        user: 'user1',
        pass: '123',
        heartbeat: {
            incoming: '30s',
            outgoing: '30s',
        },
        message_send_timeout: '5s',
        receipt_timeout: '10s',
        read_buffer_size: 4096,
        read_channel_capacity: 20,
        write_buffer_size: 4096,
        write_channel_capacity: 20,
        verbose: false
    });

     // subscribe to receive messages from '/topic/greetings' with auto ack
    let subscription = client.subscribe('/user/topic/price');


    // send a message to '/app/hello' with application/json as MIME content-type
    let payload = {"isins": ["IRO1FOLD0001", "IRT3TVAF0001"]};
    client.send('/user/topic/price/add-filter', 'application/json', JSON.stringify(payload));
    console.log('send /user/topic/price/add-filter');

    // read the message
    for (let j = 0; j < 2; j++) {
        // show the message
        const msg = subscription.read();
        console.log('msg: ' + JSON.stringify(msg));
        console.log('msg count: '+ j);
    }

    conn2cons.add(Date.now() - connAt); // Unix timestamp in milliseconds

    // disconnect from websocket
    client.disconnect();
}

export default function () {

    login();
    ws();
}