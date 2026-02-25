package pe.gob.onpe.sceorcbackend.model.dto.transmision;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransmisionResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 7249684515942055039L;

    private boolean success;
    private String message;
    private List<ActaTransmitidaDto> actasTransmitidas;
    
    public static TransmisionResponseDto getObject(String jsonString) throws  JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(jsonString, TransmisionResponseDto.class);
	}
    
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }


}
