package imagej.workflowpipes.modules;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imagej.workflow.IItemInfo;
import imagej.workflow.IModuleInfo;
import imagej.workflow.debug.PreviewInfo;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Attr;
import imagej.workflowpipes.pipesentity.Conf;
import imagej.workflowpipes.pipesentity.Connector;
import imagej.workflowpipes.pipesentity.Content;
import imagej.workflowpipes.pipesentity.Count;
import imagej.workflowpipes.pipesentity.Description;
import imagej.workflowpipes.pipesentity.Error;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Item;
import imagej.workflowpipes.pipesentity.Message;
import imagej.workflowpipes.pipesentity.Name;
import imagej.workflowpipes.pipesentity.Prop;
import imagej.workflowpipes.pipesentity.Tag;
import imagej.workflowpipes.pipesentity.Terminal;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.pipesentity.UI;
import java.io.Serializable;

public class ModuleBase extends Module {
    List<NameTypeDesc> m_nameTypeDescList = new ArrayList<NameTypeDesc>();
    IItemInfo m_inputItemInfos[];

	public ModuleBase( IModuleInfo moduleInfo )
	{
            // save inputs to get outputs later
            m_inputItemInfos = moduleInfo.getInputItemInfos();

		// populate ID
		this.id = new ID("");

		// loop through inputs and add terminals
		for ( IItemInfo inputItemInfo : moduleInfo.getInputItemInfos() )
		{
                    String inputName = inputItemInfo.getName();
                    String inputType = null;
                    switch (inputItemInfo.getType()) {
                        case STRING:
                        case INTEGER:
                        case FLOATING:
                            inputType = "text";
                            break;
                        case URL:
                            inputType = "url";
                            break;
                        case IMAGE:
                            inputType = "items";
                            break;
                    }
                    // Images become terminals, anything else is a setting in the UI.
                    if (IItemInfo.Type.IMAGE.equals(inputItemInfo.getType())) {
                        this.terminals.add( Terminal.getInputTerminal( inputType, inputName ) );
                    }
                    else {
                        m_nameTypeDescList.add(new NameTypeDesc(inputName, inputType, inputName));
                    }
		}
		
		// loop through output and add terminals
		for ( IItemInfo outputItemInfo : moduleInfo.getOutputItemInfos() )
		{
                    String outputName = outputItemInfo.getName();
                    String outputType = null;
                    switch (outputItemInfo.getType()) {
                        case STRING:
                        case INTEGER:
                        case FLOATING:
                            outputType = "text";
                            break;
                        case URL:
                            outputType = "url";
                        case IMAGE:
                            outputType = "items";
                            break;
                    }
                    this.terminals.add( Terminal.getOutputTerminal( outputType, outputName ) );
                }

                // build HTML based UI
                this.ui = getUI(m_nameTypeDescList);

		//
		this.name = new Name( moduleInfo.getName() );

		// TODO replace with single string representing the GUI type
		this.type = new Type( moduleInfo.getName() );

		this.description = new Description("TODO map me with descriptive text");

		Tag tag = new Tag("system:img");

		this.tags = Tag.getTagsArray(tag);
		
		//TODO this is to be replaced with the implementation
		this.module = "Yahoo::RSS::FetchPage";
	}

        /**
         * Returns the user inputs, as a map from name to value object.
         *
         * @param list of names
         * @return map of name to value object
         */
        public Map<String, Object> getInputs(List<String> wiredInputNames)
        {
            Map<String, Object> map = new HashMap<String, Object>();

            for (IItemInfo itemInfo : m_inputItemInfos) {
                String name = itemInfo.getName();
                if (!wiredInputNames.contains(name)) {
                    Conf conf = Conf.getConf(name, confs);
                    if (null != conf) {
                        String value = conf.getValue().getValue();
                        Object object = null;
                        switch (itemInfo.getType()) {
                            //TODO error handling, ""==value or parser fails
                            case STRING:
                                object = value;
                                break;
                            case INTEGER:
                                object = Integer.parseInt(value);
                                break;
                            case FLOATING:
                                object = Float.parseFloat(value);
                                break;
                            case URL:
                                object = value;
                                break;
                            case IMAGE:
                                break;
                        }
                        if (null != object) {
                            map.put(name, object);
                        }
                    }
                }
            }
            return map;
        }

	public void go( List<PreviewInfo> previewInfoList )
	{
            System.out.println("In ModuleBase.go()");
		// call the start method
		start();

                for ( PreviewInfo previewInfo : previewInfoList ) {
		    // add the items
		    this.items.add( new Item( previewInfo.getDesc(), previewInfo.getContent()) );
		    this.item_count.incrementCount();
		    this.count.incrementCount();

                }

    /*            for (NameTypeDesc nameTypeDesc : _nameTypeDescList) {
                    if ("url".equals(nameTypeDesc.getType())) {
                        Conf urlConf = Conf.getConf( nameTypeDesc.getName(), confs );

                        // get the url
                        String url = urlConf.getValue().getValue();

                        System.out.println("url type name " + nameTypeDesc.getName() + " desc " + nameTypeDesc.getDesc() + " " + url);
                    }
                    else {
                        Conf conf = Conf.getConf( nameTypeDesc.getName() , confs );
                        System.out.println("type " + nameTypeDesc.getType() + " name " + nameTypeDesc.getName() + " desc " + nameTypeDesc.getDesc() + " value " + conf.getValue().getValue());
                    }
                }*/

/*
		Conf urlConf = Conf.getConf( "URL", confs );
		// System.out.println("FetchPage urlConf is " + urlConf.getJSONObject() );

		// get the url
		String url = urlConf.getValue().getValue();
		// System.out.println("FetchPage conf value for URL conf is " + url );

		String s;
		String contentString = "";
		BufferedReader r = null;

		try {
			r = new BufferedReader(new InputStreamReader(new URL("http://" + url).openStream()));
		} catch (MalformedURLException e) {
			// add the error
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		} catch (IOException e) {
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		}

	    try {
			while ((s = r.readLine()) != null) {
				contentString += s;
			}
		} catch (IOException e) {
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		}
*/
		// add the content to prop //TODO: there is likely a much easier way to do this...
	//	this.props.add( new Prop( new Connector( "_OUTPUT", new Type("item"), new Attr( new Content( new Type("text"), new Count(1) ) ) ) ) );

		// System.out.println( "FetchPage results " + contentString );
//*/
   /*             String contentString = "<html><body>HELLO</body></html>";
		// add the items
		this.items.add( new Item( "content", contentString ) );
		this.item_count.incrementCount();
		this.count.incrementCount();*/


		// add self generated stats
		this.response.addStat("CACHE_HIT", new Integer(1) );
		this.response.addStat("CACHE_MISS", new Integer(2) );

		// call stop
		stop();
	}

        /**
         * For the given list of name, type, and description, build the UI HTML.
         *
         * @param nameTypeDescList
         * @return
         */
        private UI getUI(List<NameTypeDesc> nameTypeDescList) {
            StringBuilder stringBuilder = new StringBuilder();
            for (NameTypeDesc nameTypeDesc : nameTypeDescList) {
                stringBuilder.append("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>");
                stringBuilder.append(nameTypeDesc.getDesc());
                stringBuilder.append(": </label><input name=\"");
                stringBuilder.append(nameTypeDesc.getName());
                stringBuilder.append("\" type=\"");
                stringBuilder.append(nameTypeDesc.getType());
                stringBuilder.append("\" required=\"true\"/>\n\t\t</div> ");
            }
            stringBuilder.append(" \n\t\t");
            return new UI(stringBuilder.toString());
        }

        /**
         * Keeps track of associated name, type, and description.
         *
         */
        class NameTypeDesc implements Serializable {
            private final String _name;
            private final String _type;
            private final String _desc;

            NameTypeDesc(String name, String type, String desc) {
                _name = name;
                _type = type;
                _desc = desc;
            }

            String getName() {
                return _name;
            }

            String getType() {
                return _type;
            }

            String getDesc() {
                return _desc;
            }
        }

}
