package jp.beacrew.locotester;

public class ListItem {

    private int imageId;
    private String text;
    private String subText;
    private String cluseterName;

    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public String getCluseterName() {
        return cluseterName;
    }

    public void setCluseterName(String cluseterName) {
        this.cluseterName = cluseterName;
    }
}

