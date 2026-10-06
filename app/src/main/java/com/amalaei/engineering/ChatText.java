package com.amalaei.engineering;
import java.util.*;
import java.util.regex.*;
import java.time.*;
/** Recognizes common plain text exports; retains multiline messages and skips system lines. */
final class ChatText {
    static final class Message {String sender,body;long at;boolean dated=false;Message(String sender,String body,long at){this.sender=sender;this.body=body;this.at=at;}}
    static LinkedHashMap<String,ArrayList<Message>> parse(String text,long fallback,boolean monthFirst){
        LinkedHashMap<String,ArrayList<Message>> result=new LinkedHashMap<>();Message current=null;
        Pattern header=Pattern.compile("^\\[?(\\d{1,4}[./-]\\d{1,2}[./-]\\d{2,4}),?\\s+([0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)(?:\\s*([APap][Mm]|ص|م))?\\]?\\s*(?:-\\s*)?([^:]{1,100}):\\s*(.*)$");
        Pattern system=Pattern.compile("^\\[?\\d{1,4}[./-]\\d{1,2}[./-]\\d{2,4}[,\\s].*");
        for(String line:Analysis.digits(text).replace("\u200e","").replace("\u200f","").replace("\u202f"," ").replace("\u00a0"," ").split("\\r?\\n")){
            Matcher m=header.matcher(line);if(m.matches()){
                long at=fallback;boolean valid=false;try{String[] parts=m.group(1).split("[./-]");int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]),c=Integer.parseInt(parts[2]);int year=parts[0].length()==4?a:c<100?2000+c:c;int month=parts[0].length()==4?b:monthFirst?a:b;int day=parts[0].length()==4?c:monthFirst?b:a;
                    String[] clock=m.group(2).split(":");int h=Integer.parseInt(clock[0]);String suffix=m.group(3);if(suffix!=null&&(suffix.equals("م")||suffix.equalsIgnoreCase("PM"))&&h<12)h+=12;if(suffix!=null&&(suffix.equals("ص")||suffix.equalsIgnoreCase("AM"))&&h==12)h=0;at=LocalDate.of(year,month,day).atTime(h,Integer.parseInt(clock[1])).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();valid=true;
                }catch(Exception invalid){}
                String sender=m.group(4).trim();current=new Message(sender,m.group(5),at);current.dated=valid;if(!result.containsKey(sender))result.put(sender,new ArrayList<>());result.get(sender).add(current);
            }else if(system.matcher(line).matches()){current=null;}else if(current!=null){current.body+="\n"+line;}
        }
        return result;
    }
}
