package com.network.client.io;

import com.network.client.view.ClientView;
import com.network.model.FileInfo;
import com.network.model.LoginFormData;
import com.network.model.Request;
import com.network.model.Response;
import com.network.model.Session;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketConnector {
    
    public final int SIZE_OF_SPLIT_PART = 1024; // bytes
    ClientView clientView;
   
    public Response login(String host, int port, LoginFormData formData) {
        try (Socket socket = new Socket(host, port);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            // send req contains username, password
            Request<LoginFormData> request = new Request<>();
            request.type = Request.Type.LOGIN;
            request.session = null;
            request.data = formData;
            output.writeObject(request);
            // receive Resp contains session
            return (Response) input.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SocketConnector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
