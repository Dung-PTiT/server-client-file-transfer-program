package com.network.client.controller;

import com.network.client.io.SocketConnector;
import com.network.client.view.ClientView;
import com.network.client.view.LoginView;
import com.network.model.FileInfo;
import com.network.model.LoginFormData;
import com.network.model.Request;
import com.network.model.Response;
import com.network.model.Session;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class ClientController {

    public LoginView loginView;
    public ClientView clientView;

    public String host;
    public int port;
    public Session session;

    public SocketConnector socketConnector;

    public final int SIZE_OF_SPLIT_PART = 1024;

    private SimulatedActivity activity;
    private int current;
    private int target;
    public ClientController() {
        loginView = new LoginView(this);
        clientView = new ClientView(this);
        socketConnector = new SocketConnector();
        final int MAX = 100;
    }

    public void run() {
        loginView.setVisible(true);
    }

    public boolean login(LoginFormData loginFormData) {
        Response<Session> response = socketConnector.login(host, port, loginFormData);
        if (response.data != null && response.data.authenticated) {
            this.session = response.data;
            loginView.setVisible(false);
            clientView.setVisible(true);
            return true;
        } else {
            JOptionPane.showMessageDialog(loginView.getRootPane(), "Login failed!");
            return false;
        }
    }

    public void sendFiles(List<File> selectedFiles) {
        selectedFiles.forEach((file) -> {
            new SimulatedActivity(file).execute();
        });
    }

    class SimulatedActivity extends SwingWorker<Void, Integer> {

        File file;

        public SimulatedActivity(File file) {
            current = 0;
            target = 100;
            this.file = file;
        }

        protected Void doInBackground() throws Exception {
            send(this.file);
            return null;
        }

        protected void process(List<Integer> chunks) {
            clientView.getjProgressBar5().setValue(chunks.get(0));

        }

        public boolean send(File file) {
            try (Socket socket = new Socket(host, port);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                Request<FileInfo> request = new Request<>();
                request.type = Request.Type.SEND_FILE;
                request.session = session;
                FileInfo fileInfo = new FileInfo();
                fileInfo.name = file.getName();
                fileInfo.size = file.length();
                request.data = fileInfo;
                oos.writeObject(request);
                oos.flush();
              
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[SIZE_OF_SPLIT_PART];
                int numberOfReadBytes;
                long size = file.length();

                long numberOfSentBytes = 0;
                while ((numberOfReadBytes = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, numberOfReadBytes);
                    numberOfSentBytes += numberOfReadBytes;

                    current = (int) (((double) numberOfSentBytes / (double) size) * 100);
                    publish(current);
                    if (numberOfReadBytes < SIZE_OF_SPLIT_PART) {
                        break;
                    }
                }
                fis.close();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(SocketConnector.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }
}
