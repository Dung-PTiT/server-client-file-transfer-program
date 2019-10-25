package com.network.server;

import com.network.model.Session;
import com.network.model.FileInfo;
import com.network.model.LoginFormData;
import com.network.model.Request;
import com.network.model.Response;
import com.network.model.ServerConfig;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends Thread {

    private ServerConfig serverConfig;
    private ServerSocket server;
    private List<Session> authentications;

    public ServerProcess(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.authentications = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(this.serverConfig.port);
            System.out.println("Server has started with port " + this.serverConfig.port + "! Waiting to connect...");
            try {
                while (!this.isInterrupted()) {
                    Socket socket = server.accept();
                    (new SocketThread(socket, serverConfig, authentications)).start();
                }
            } catch (IOException e) {
                System.out.println("Server can't wait to accept from client now");
            }

        } catch (IOException e) {
            System.out.println("Can't start server!");
        }
    }

    @Override
    public void interrupt() {
        try {
            System.out.println("Server is closing...");
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        authentications.clear();
        super.interrupt();
        System.out.println("Server has shut down");
    }

    private static class SocketThread extends Thread {

        public final int SIZE_OF_SPLIT_PART = 1024; // bytes

        private Socket socket;
        private ServerConfig serverConfig;
        private List<Session> authentications;

        public SocketThread(Socket socket, ServerConfig serverConfig, List<Session> authentications) {
            this.socket = socket;
            this.serverConfig = serverConfig;
            this.authentications = authentications;
            System.out.println(socket.getInetAddress().getHostAddress() + " has connected.");
        }

        @Override
        public void run() {
            Session session = new Session();
            try (DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
                    ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                    DataInputStream dataInput = new DataInputStream(socket.getInputStream());
                    ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream())) {
                Request request = (Request) objectInput.readObject();
                if (request.type == Request.Type.LOGIN) {
                    LoginFormData loginFormData = (LoginFormData) request.data;
                    if (this.serverConfig.username.equals(loginFormData.username) && this.serverConfig.password.equals(loginFormData.password)) {
                        session.authenticated = true;
                        session.sessionToken = UUID.randomUUID().toString();
                        session.clientIP = socket.getInetAddress().getHostAddress();
                        this.authentications.add(session);
                    } else {
                        session.authenticated = false;
                        session.sessionToken = null;
                    }
                    Response<Session> response = new Response<>();
                    response.data = session;
                    objectOutput.writeObject(response);
                    objectOutput.flush();
                    if (session.authenticated) {
                        System.out.println(socket.getInetAddress().getHostAddress() + " has logged in.");
                    }
                } else if (request.type == Request.Type.SEND_FILE) {
                    FileInfo fileInfo = (FileInfo) request.data;

                    File receivedFile = new File(serverConfig.uploadDir + "\\" + fileInfo.name);
                    FileOutputStream fos = new FileOutputStream(receivedFile);

                    byte[] buffer = new byte[SIZE_OF_SPLIT_PART];
                    long numberOfReadBytes;
                    long numberOfReceivedBytes = 0;

                    while ((numberOfReadBytes = dataInput.read(buffer)) != -1) {
                        fos.write(buffer);
                        numberOfReceivedBytes += numberOfReadBytes;
                        if (numberOfReadBytes < SIZE_OF_SPLIT_PART) {
                            break;
                        }
                    }
                    fos.close();

                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ServerProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            this.authentications.removeIf((t) -> {
                return session.sessionToken.equals(t.sessionToken);
            });
            System.out.println(socket.getInetAddress().getHostAddress() + " has disconnected.");
        }
    }
}
