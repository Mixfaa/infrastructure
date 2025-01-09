Service, built for establishing connections and messaging between apps(i expected gamehacks),

Uses default sockets for communication

Has next operations:
- AUTHENTICATE params: username: String, password: String code 0
- REGISTER params: username: String, password: String code 1
- RENT_CHANNEL returns: channel name:String code 2
- RENT_PUBLIC_CHANNEL params: name:String returns: channel name:String code 3
- CLOSE_CHANNEL params: channel name:String code 4
- SUBSCRIBE_CHANNEL params: channel name:String code 5
- UNSUBSCRIBE_CHANNEL params: channel name:String code 6
- SEND_TO_CHANNEL params: channel name:String, payload:ByteArray code 7
- LIST_PUBLIC_CHANNELS params: query:String, page:Int returns: List<String> channel names code 8

some operations can return error, error model contains only msg(String) field 

request polictics is next:

separator byte = ':'
max request size = 512

request structure = OPERATION_CODE:PARAM1:PARAM2:...
