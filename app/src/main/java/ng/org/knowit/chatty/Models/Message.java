package ng.org.knowit.chatty.Models;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class Message implements IMessage {

    private String Id;

    private String Text;

    private Date CreatedAt;

    private User mUser;

    public Message(String Id, String Text, Date CreatedAt, User user){
        this.Id = Id;
        this.Text = Text;
        this.CreatedAt = CreatedAt;
        this.mUser = user;
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getText() {
        return Text;
    }

    @Override
    public IUser getUser() {
        return mUser;
    }

    @Override
    public Date getCreatedAt() {
        return CreatedAt;
    }
}
