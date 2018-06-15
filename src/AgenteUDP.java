import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.management.ManagementFactory;
import java.net.*;


public class AgenteUDP {
    String group_name;
    private InetAddress group;
    private MulticastSocket socket = null;
    private int port;
    private byte[] bufin = new byte[1024];
    OperatingSystemMXBean bean;
    HmacSha1Signature codec;
        


    public AgenteUDP(String group, int port) throws Exception {
        this.group_name = group;
        this.group = InetAddress.getByName(group);
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(this.group);
        this.port = port;
        this.bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.codec = new HmacSha1Signature();
    }


    public void run() throws IOException {

        while (true) {
            
            byte[] bufin = new byte[1024];
            DatagramPacket recv_packet =
                    new DatagramPacket(bufin, bufin.length);
            socket.receive(recv_packet);
            String received =
                    new String(recv_packet.getData(),0,recv_packet.getLength());
            System.out.println("Received:"+received);
            if ("end".equals(received)) {
                break;
            }else if("Probe".equals(received)){
                double cpu =
                        this.bean.getProcessCpuLoad();
                long ram =
                        this.bean.getFreePhysicalMemorySize();

                String str =
                        String.format("%.10f", cpu)+" , "+String.format("%d",ram);
                String hash =
                        codec.calculateRFC2104HMAC(str, codec.key);
                String probe =
                        hash + ":" +str;
                byte[] bufout =
                        probe.getBytes();
                System.out.println("Sent: "+probe);
                int delay = ThreadLocalRandom.current().nextInt(0, 10 + 1);
                try{
                    Thread.sleep(delay);
                    DatagramPacket send_packet =
                            new DatagramPacket(bufout, bufout.length,
                                    recv_packet.getAddress(), recv_packet.getPort());
                    socket.send(send_packet);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        socket.leaveGroup(group);
        socket.close();


    }







    public static void main(String[] args){
        try {
            new AgenteUDP("239.8.8.8",8888).run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
