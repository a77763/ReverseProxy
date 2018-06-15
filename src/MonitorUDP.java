import java.io.IOException;
import java.net.*;

public class MonitorUDP implements Runnable{
    private TabelaDeEstado tabela;
    private InetAddress group = null;
    private DatagramSocket socket = null;
    private byte[] buf;
    private HmacSha1Signature codec;

    public MonitorUDP(TabelaDeEstado tabela) throws Exception {
        this.tabela = tabela;
        this.socket = new DatagramSocket();
        this.group = InetAddress.getByName("239.8.8.8");
        this.buf = "Probe".getBytes();
        this.codec = new HmacSha1Signature();
    }





    @Override
    public void run(){
        while(true) try {
            DatagramPacket send_packet;
            long rtt;

            send_packet = new DatagramPacket(buf, buf.length, group, 8888);
            rtt = System.currentTimeMillis();
            socket.send(send_packet);
            System.out.println("Sent:Probe - Time:"+rtt);
            socket.setSoTimeout(3000);
            
            while (true) {
                DatagramPacket recv_packet;
                String[] data;
                byte[] bufin = new byte[1024];
                long ram;
                double cpu;
                
                recv_packet = new DatagramPacket(bufin, bufin.length);
                socket.receive(recv_packet);
                rtt = System.currentTimeMillis() - rtt;
                data = new String(recv_packet.getData(), 0, recv_packet.getLength()).split(":",2);
                ram = 0;
                cpu = 0;
                if (data.length == 2 && codec.calculateRFC2104HMAC(data[1], codec.key).equals(data[0])) {
                    System.out.println("Receive:Answer");
                    String[] probe = data[1].split(" , ");
                    cpu = Double.parseDouble(probe[0]);
                    ram = Long.parseLong(probe[1]);
                    tabela.adicionaEstado(
                            ram,
                            cpu,
                            rtt,
                            recv_packet.getAddress(),
                            recv_packet.getPort()

                    );
                }
                System.out.println(tabela.toString());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
