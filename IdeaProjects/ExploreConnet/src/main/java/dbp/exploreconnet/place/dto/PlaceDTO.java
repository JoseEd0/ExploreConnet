package dbp.exploreconnet.place.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceDto {
    private Long id;
    private String name;
    private String address;

    public PlaceDto(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
