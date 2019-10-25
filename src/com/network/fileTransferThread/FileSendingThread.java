package com.network.fileTransferThread;

import com.network.client.io.SocketConnector;
import com.network.model.FileInfo;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSendingThread extends Thread{
    private List<String> fileNameList;
    private List<String> fileDirList;
    private List<Long> fileSizeList;
    private List<FileInfo> fileList;
    private Socket socket;

    public FileSendingThread(List<String> fileNameList, List<String> fileDirList, List<Long> fileSizeList, List<FileInfo> fileList, Socket socket) {
        this.fileNameList = fileNameList;
        this.fileDirList = fileDirList;
        this.fileSizeList = fileSizeList;
        this.fileList = fileList;
        this.socket = socket;
    }

    @Override
    public void run() {
         try {           
            DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            for (int i = 0; i < fileDirList.size(); i++) {
                String path = (String) fileDirList.get(i);
                byte[] byteArray = Files.readAllBytes(Paths.get(path));
                long fileSize = (long) fileSizeList.get(i);
                String fileName = (String) fileNameList.get(i);              
                //FileInfo fi = new FileInfo(fileName, fileSize);
                //objectOutputStream.writeObject(fi);
                objectOutputStream.flush();
                dataOutputStream.write(byteArray);
                dataOutputStream.flush();
            }
            dataOutputStream.close();
            objectOutputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void start() {
        super.start(); 
    }   
}
