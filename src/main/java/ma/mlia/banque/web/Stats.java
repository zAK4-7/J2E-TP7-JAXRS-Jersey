package ma.mlia.banque.web;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Small DTO returned by the statistics endpoint.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats implements Serializable {

    private static final long serialVersionUID = 1L;

    private long accountCount;
    private double totalBalance;
}
