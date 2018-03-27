package gnu.io;

public abstract interface SerialPortService
{
	public abstract CommPort open(CommDriver paramCommDriver, String paramString1, String paramString2, int paramInt)
			throws PortInUseException;
}