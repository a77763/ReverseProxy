import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MonitorTCP implements Runnable{
    private ServerSocket serverSocket;
    private int port;
    private TabelaDeEstado tab;
    public MonitorTCP(int port, TabelaDeEstado tab) throws IOException {
        this.port        = port;
        this.serverSocket = new ServerSocket(port);
        this.tab = tab;

    }

    @Override
    public void run() {
        while(true){
            Socket client = null;
            try {
                client = serverSocket.accept();
                boolean flag = true;
                while(flag){
                
                    InetAddress address = tab.melhorEstado();
                    System.out.println(address);
                    if(address != null){
                        flag = false;
                        System.out.println("Espera...");
                        new Thread(new Link(address, client,tab)).run();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


class Link implements Runnable{
    Socket cliente_sock;
    BufferedReader cliente_out;
    PrintWriter cliente_in;
    TabelaDeEstado tb;
    InetAddress ip;
    int size;

    public Link(InetAddress ip, Socket client, TabelaDeEstado tb) throws IOException {
        System.out.println(ip);
        this.cliente_sock = client;
        this.cliente_out = new BufferedReader(new InputStreamReader(cliente_sock.getInputStream()));
        this.cliente_in = new PrintWriter(cliente_sock.getOutputStream(), true);
        this.ip = ip; 
        this.tb = tb;
        size=0;
    }


    @Override
    public void run(){
        try {
            Socket http_sock = new Socket(this.ip,80);
            BufferedReader http_out = new BufferedReader(new InputStreamReader(http_sock.getInputStream()));
            PrintWriter http_in = new PrintWriter(http_sock.getOutputStream(), true);
            LinkReader lr = new LinkReader(this.ip, http_sock,this.tb,http_out,this.cliente_in);
            Thread t = new Thread(lr);
            t.start();
            
            String line;
            
            long time1 = System.currentTimeMillis();
            
            while( (line = cliente_out.readLine()) != null) {
                http_in.println(line);                 
                size+=line.length();
            }
            t.join();
            time1 = System.currentTimeMillis()-time1;
            size+= lr.size;
            tb.upBW(this.ip, size *1000/time1);


        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
class LinkReader implements Runnable{
    Socket http_sock;
    BufferedReader http_out;
    PrintWriter client_in;
    TabelaDeEstado tb;
    InetAddress ip;
    int size;

    public LinkReader(InetAddress ip, Socket http, TabelaDeEstado tb, BufferedReader out, PrintWriter in) throws IOException {
        this.http_sock = http;
        this.http_out = out;
        this.client_in = in;
        this.ip = ip; 
        this.tb = tb;
        this.size = 0;
    }


    @Override
    public void run(){
        try {
            String line;
            System.out.println("reader");
            while( (line = http_out.readLine()) != null) {
                client_in.println(line);
                size+=line.length();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
