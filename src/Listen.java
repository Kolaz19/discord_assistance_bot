import jdk.jfr.Event;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.channels.Channel;
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
        if (la_content[0].equals("!commands") && (la_content.length == 1)) {
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

            int lv_numberOfMessages;
            try {
                lv_numberOfMessages = Integer.valueOf(la_content[1]);
            } catch (Exception ex) {
                return;
            }

            List<Message> la_history = this.getPastMessages(lv_numberOfMessages,lr_channel,ir_event);
            Member lr_member = null;

            try {
                    lr_member = mr_guild.getMembersByEffectiveName(la_content[2], true).get(0);
            } catch (Exception ex) {

            }

            for (Message lr_currentItem : la_history) {
                if ((lr_member == lr_currentItem.getMember()) || (la_content.length == 2)) {
                    lr_currentItem.delete().queue();
                }
            }
        } else if (la_content[0].equals("!list")) {
            List<Role> la_roles;
            try {
                la_roles = mr_guild.getRolesByName(la_content[1], true);
            } catch (Exception ex) {
                return;
            }
            if (la_roles.isEmpty()) {
                return;
            }
            List<Member> la_members = mr_guild.getMembersWithRoles(la_roles);
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


    public List<Message> getPastMessages (int iv_numberOfMessages, MessageChannel ir_channel, MessageReceivedEvent ir_event) {

        if (iv_numberOfMessages > 100) {
            iv_numberOfMessages = 99;
        }

        List<Message> la_history;
        MessageHistory.MessageRetrieveAction lr_history = ir_channel.getHistoryBefore(ir_event.getMessageId(),iv_numberOfMessages);
        la_history = lr_history.complete().getRetrievedHistory();
        return la_history;
    }

}
