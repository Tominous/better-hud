package tk.nukeduck.hud.util;

public interface ISaveLoad {
	/** @return A {@link #load(String)}-compatible
	 * representation of this object */
	public abstract String save();

	/** @param save A representation of the object to load,
	 * generated by {@link #save()} */
	public abstract void load(String save);
}
