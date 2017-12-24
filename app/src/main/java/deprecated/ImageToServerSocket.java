package deprecated;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ImageToServerSocket {
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.0.2.2";
    private Socket socket;

    ImageToServerSocket imageToServerSocket;
    // new Thread(new ClientThread()).start();

    private ImageToServerSocket(){

    }

    public ImageToServerSocket getInstance(){
        if (imageToServerSocket != null){
            return imageToServerSocket;
        } else {
            imageToServerSocket = new ImageToServerSocket();
            return imageToServerSocket;
        }
    }


    public void writeFileToServer(byte [] data) throws IOException {
        Socket socket=new Socket(SERVER_IP, SERVERPORT);
        OutputStream socketOutputStream = socket.getOutputStream();
        socketOutputStream.write(data);
    }
}
