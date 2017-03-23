import java.util.UUID;

/**
 * Created by Sheena on 19/03/2017.
 */

public class PositionCoords {
    private UUID mId;
    private String mCoords;

    public PositionCoords() {
        // Generate unique identifier
        mId = UUID.randomUUID();
    }

    public UUID getId() {
        return mId;
    }

    public String getCoords() {
        return mCoords;
    }

    public void setCoords(String coords) {
        mCoords = coords;
    }
}
