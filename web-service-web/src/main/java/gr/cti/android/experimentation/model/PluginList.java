package gr.cti.android.experimentation.model;

import gr.cti.android.experimentation.model.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginList
{
	private List<Plugin> plugList;
	
	public PluginList()
	{
		plugList = new ArrayList<>();
	}
	
	public void setPluginList(List<Plugin> plugList)
	{
		this.plugList = plugList;
	}
	
	public List<Plugin> getPluginList()
	{
		return this.plugList;
	}
}