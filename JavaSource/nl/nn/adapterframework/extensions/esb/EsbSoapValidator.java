/*
 * $Log: EsbSoapValidator.java,v $
 * Revision 1.5  2012-08-23 11:57:43  m00f069
 * Updates from Michiel
 *
 * Revision 1.4  2012/05/08 15:55:38  Jaco de Groot <jaco.de.groot@ibissource.org>
 * Fix wrong namespace prefix in wsdl:part element.
 *
 * Revision 1.3  2012/04/12 08:50:02  Jaco de Groot <jaco.de.groot@ibissource.org>
 * CommonMessageHeader.xsd namespace has changed (uri -> http)
 *
 */
package nl.nn.adapterframework.extensions.esb;

import java.util.*;

import javax.xml.namespace.QName;

import nl.nn.adapterframework.soap.SoapValidator;

/**
 * This is a SoapValidator, but it presupposes ESB wrapping of the body.
 * @author Michiel Meeuwissen
 * @author Jaco de Groot
 * @version Id
 */
public class EsbSoapValidator extends SoapValidator {

    private static class HeaderInformation {
        final String xmlns;
        final String xsd;
        final QName tag;
        HeaderInformation(String xmlns, String xsd) {
            this.xmlns = xmlns;
            this.xsd   = xsd;
            this.tag   = new QName(this.xmlns, "MessageHeader");
        }

    }

    private static final Map<EsbSoapWrapperPipe.Mode, HeaderInformation> GENERIC_HEADER;
    static {
        Map<EsbSoapWrapperPipe.Mode, HeaderInformation> temp = new HashMap<EsbSoapWrapperPipe.Mode, HeaderInformation>();
        temp.put(EsbSoapWrapperPipe.Mode.REG, new HeaderInformation("http://nn.nl/XSD/Generic/MessageHeader/1", "/xml/xsd/CommonMessageHeader.xsd"));
        temp.put(EsbSoapWrapperPipe.Mode.I2T, new HeaderInformation("http://nn.nl/XSD/Generic/MessageHeader/1", "/xml/xsd/CommonMessageHeader.xsd"));
        temp.put(EsbSoapWrapperPipe.Mode.BIS, new HeaderInformation("http://www.ing.com/CSP/XSD/General/Message_2", "/xml/xsd/cspXML_2.xsd"));
        GENERIC_HEADER = new EnumMap<EsbSoapWrapperPipe.Mode, HeaderInformation>(temp);
    }

    // This is unused now, we use to to have an extra tag on the output.
    //private static final QName  GENERIC_RESULT_TAG     = new QName(GENERIC_HEADER_XMLNS, "Result");

    public static enum Direction {
        INPUT,
        OUTPUT
    }

    private Direction direction = null;
    private EsbSoapWrapperPipe.Mode mode = EsbSoapWrapperPipe.Mode.REG;
    private String explicitSchemaLocation = null;


    @Override
    public void setSchemaLocation(String schemaLocation) {
        super.setSchemaLocation(GENERIC_HEADER.get(mode).xmlns + " " + GENERIC_HEADER.get(mode).xsd + " " + schemaLocation);
        explicitSchemaLocation = schemaLocation;
    }

    @Override
    protected int getDefaultNamespaceIndex() {
        return super.getDefaultNamespaceIndex() + 1;
    }

    @Override
    public void setSoapHeader(String bodyTags) {
        throw new IllegalArgumentException("Esb soap is unmodifiable, it is: " + getSoapHeaderTags());
    }

    @Override
    public Collection<QName> getSoapHeaderTags() {
        return Collections.singleton(GENERIC_HEADER.get(mode).tag);
    }

    public String getDirection() {
        return direction.toString();
    }

    public void setDirection(String direction) {
        this.direction = Direction.valueOf(direction.toUpperCase());
    }

    public void setMode(String mode) { // Why does PropertyUtil not understand enums?
        this.mode = EsbSoapWrapperPipe.Mode.valueOf(mode.toUpperCase());
        if (explicitSchemaLocation != null) setSchemaLocation(explicitSchemaLocation);
    }

    public String getMode() {
        return mode.toString();
    }
}