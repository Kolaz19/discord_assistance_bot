import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AssistanceBot {

    public static JDA gr_jda;

    public static void main (String[] args) throws LoginException, InterruptedException {

        gr_jda = JDABuilder.createDefault(AssistanceBot.getParameter("server.csv","server_token"))
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .enableIntents(GatewayIntent.GUILD_MEMBERS)
                            .build();
        //Wait till connection is ready
        gr_jda.awaitReady();
        gr_jda.addEventListener(new Listen(gr_jda));
    }


    public static String getParameter (String iv_fileName,String iv_parameterName) {
        String lr_backString = "";
        try {
            Scanner lr_scanner = new Scanner(new File(iv_fileName));
            while (lr_scanner.hasNextLine()) {
                String[] lr_tempoString = lr_scanner.nextLine().split("=");
                if (lr_tempoString[0].equals(iv_parameterName)) {
                    lr_backString = lr_tempoString[1];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return lr_backString;
    }
}