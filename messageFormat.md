This file explains the format of messages between client and server.

### Signing up messages

After submitting the signing form, the client send to the server all user's informations.

Then the response of the server should have the following format :

      object :
            _connect : 1 if a connection was established between the server and the client
                        0 otherwise
            _success : 1 if succed
                        0 otherwise
            _error : The message error
            _user : JSONobject wich contains user's informations
    