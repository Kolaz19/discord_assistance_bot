import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Listen extends ListenerAdapter {

    private JDA mr_jda;
    private Guild mr_guild;
    private long mv_serverId;
    private long mv_channelIdBotCommand;

    Listen(JDA ir_jda) {
        mr_jda = ir_jda;
        mv_serverId = Long.parseLong(AssistanceBot.getParameter("server.csv","server_id"));
        mv_channelIdBotCommand = Long.parseLong(AssistanceBot.getParameter("server.csv","botchannel_id"));
        mr_guild = mr_jda.getGuildById(mv_serverId);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent ir_event) {

        Message lr_message = ir_event.getMessage();
        String lr_content = lr_message.getContentRaw();
        String[] la_content = lr_content.split(" ");
        MessageChannel lr_channel = ir_event.getChannel();
        String lv_user = ir_event.getAuthor().getName();
        String lv_firstChar = lr_content.substring(0,1);



        //If user posts in channel "botcommand", but it is no botcommand -> delete
        if (!(ir_event.getAuthor().isBot()) && (ir_event.getTextChannel() == mr_jda.getTextChannelById(mv_channelIdBotCommand)) && !(lv_firstChar.equals("!"))) {
            lr_message.delete().queue();
            return;
            //Delete message in current channel and post in botChannel if message is  botcommand
        } else if ((lv_firstChar.equals("!")) && (ir_event.getTextChannel() != mr_jda.getTextChannelById(mv_channelIdBotCommand))) {
            lr_message.delete().queue();
            String lv_messageInCommandChannel = "```css" + "\n" + lr_content + "   [" + lv_user + "]" + "\n" + "```";
            mr_jda.getTextChannelById(mv_channelIdBotCommand).sendMessage(lv_messageInCommandChannel).queue();
        }



        //commands
        if (la_content[0].equals("!commands")) {
            try {
                Scanner lr_scanner = new Scanner(new File("commands.csv"));
                String lv_outputCommands = lr_scanner.nextLine() + "\n";
                while (lr_scanner.hasNextLine()) {
                    lv_outputCommands += lr_scanner.nextLine() + "\n";
                }
                lr_channel.sendMessage(lv_outputCommands).queue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (la_content[0].equals("!clear")) {
            //Check if user is MOD
            if (!this.checkRole(ir_event.getMember(),AssistanceBot.getParameter("server.csv","name_of_MOD_role"))) {
                return;
            }
            int lv_messagesToRetrieve = Integer.parseInt(la_content[1]);
            List<Message> la_history;
            MessageHistory.MessageRetrieveAction lr_history = lr_channel.getHistoryBefore(ir_event.getMessageId(),lv_messagesToRetrieve);
            la_history = lr_history.complete().getRetrievedHistory();

            for (Message lr_currentItem : la_history) {
                lr_currentItem.delete().queue();
            }
        } else if (la_content[0].equals("!list")) {
            List<Member> la_members = mr_guild.getMembersWithRoles(mr_guild.getRolesByName(la_content[1],true));
            String lv_output = "";
            for (Member lr_currentItem : la_members) {
                lv_output += lr_currentItem.getEffectiveName() + "\n";
            }
            lr_channel.sendMessage(lv_output).queue();
        }
    }

    public boolean checkRole (Member iv_member,String iv_nameOfRole) {
        boolean lv_isAuthorized = false;
        List<Role> la_roles =  iv_member.getRoles();
        for (Role lv_currentItem : la_roles)  {
            if (lv_currentItem.getName().equals(iv_nameOfRole)) {
                lv_isAuthorized = true;
            }
        }
        return lv_isAuthorized;
    }

}
