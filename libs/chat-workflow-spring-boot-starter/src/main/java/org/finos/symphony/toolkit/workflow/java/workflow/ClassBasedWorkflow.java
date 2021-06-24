package org.finos.symphony.toolkit.workflow.java.workflow;

import org.finos.symphony.toolkit.workflow.AbstractWorkflow;
import org.finos.symphony.toolkit.workflow.content.Addressable;
import org.finos.symphony.toolkit.workflow.content.Room;
import org.finos.symphony.toolkit.workflow.content.User;
import org.finos.symphony.toolkit.workflow.form.Button;
import org.finos.symphony.toolkit.workflow.form.Button.Type;
import org.finos.symphony.toolkit.workflow.form.ButtonList;
import org.finos.symphony.toolkit.workflow.java.Exposed;
import org.finos.symphony.toolkit.workflow.java.Work;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassBasedWorkflow extends AbstractWorkflow implements ConfigurableWorkflow {
	
	public static final String WF_EDIT = "wf-edit";
	private List<Class<?>> workflowClasses = new ArrayList<>();
	private Map<String, Method> methods = new HashMap<>();
	
	public ClassBasedWorkflow(String namespace) {
		this(namespace, Collections.emptyList(), Collections.emptyList());
	}
	
	public ClassBasedWorkflow(String namespace, List<User> admins, List<Room> keyRooms) {
		super(namespace, admins, keyRooms); 
	}
	
	public class ClassBasedCommandDescription implements CommandDescription {
		
		private Method m;
		private Exposed e;
		
		public ClassBasedCommandDescription(Method m) {
			this.m = m;
			this.e = m.getAnnotation(Exposed.class);
		}

		@Override
		public String getName() {
			return m.getName();
		}

		@Override
		public String getDescription() {
			return ClassBasedWorkflow.getDescription(m);
		}
		
		public Method getMethod() {
			return m;
		}

		@Override
		public boolean addToHelp() {
			return e.addToHelp();
		}

		@Override
		public boolean isButton() {
			return e.isButton();
		}

		@Override
		public boolean isMessage() {
			return e.isMessage();
		}
		
	}

	@Override
	public List<CommandDescription> getCommands(Addressable r) {
		return methods.entrySet().stream()
			.filter(e -> validCommandInAddressable(e.getValue(), r))
			.map(method -> new ClassBasedCommandDescription(method.getValue()))
			.collect(Collectors.toList());
	}
	
	public void addClass(Class<?> e) {
		matchingMethods(e, null).stream()
			.forEach(m -> {
				if (methods.containsKey(m.getName())) {
					throw new UnsupportedOperationException("Methods clash: "+m+" with "+methods.get(m.getName()));					
				} 
				methods.put(m.getName(), m);
			});
		workflowClasses.add(e);
	}
	
	private List<Method> matchingMethods(Class<?> c, Addressable a) {
		if ((c == null) || (c == Object.class)) {
			return Collections.emptyList();
		}
		
		List<Method> out = new ArrayList<>();
		
		for (Method m : c.getDeclaredMethods()) {
			if (m.getAnnotation(Exposed.class) != null) {
				if (validCommandInAddressable(m, a)) {
					out.add(m);
				}
			}
		}
		
		out.addAll(matchingMethods(c.getSuperclass(), a));
		return out;
	}
	
	@Override
	public ButtonList gatherButtons(Object out, Addressable r) {
		ButtonList buttons = new ButtonList();
		if (out.getClass().isAnnotationPresent(Work.class)) {
			Work w = out.getClass().getAnnotation(Work.class);
			if (w.editable()) {
				buttons.add(new Button(WF_EDIT, Type.ACTION, "Edit"));
			}
		}
		
		List<Method> cms = matchingMethods(out.getClass(), r);
		if (cms != null) {
			for (Method method : cms) {
				if (!Modifier.isStatic(method.getModifiers())) {
					CommandDescription cd = new ClassBasedCommandDescription(method);
					if (cd.isButton()) {
						buttons.add(new Button(method.getName(), Type.ACTION, method.getName()));
					}
				}
			}
		}
		
		return buttons;
	}

	@Override
	public String getName(Class<?> c) {
		Work w = c.getAnnotation(Work.class);
		if (w != null) {
			return w.name();
		} else {
			return c.getName();
		}
	}
	
	@Override
	public String getInstructions(Class<?> c) {
		Work w = c.getAnnotation(Work.class);
		return w.instructions();
	}
	
	public static String getDescription(Method m) {
		Exposed e = m.getAnnotation(Exposed.class);
		return e.description();
	}
	
	public boolean validCommandInAddressable(Method value, Addressable a) {
		if (a == null) {
			return true;
		} else {
			Exposed e = value.getAnnotation(Exposed.class);
			if (e.rooms().length == 0) {
				return true;
			} else if (a instanceof Room) {
				Room r = (Room) a;
				for (int i = 0; i < e.rooms().length; i++) {
					if (e.rooms()[i].equals(r.getRoomName())) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	@Override
	public List<Class<?>> getDataTypes() {
		return workflowClasses;
	}

	public Method getMethodFor(String commandName) {
		return methods.entrySet().parallelStream().filter(entry -> commandName.equalsIgnoreCase(entry.getKey())).map(Map.Entry::getValue).findFirst().get();
	}
}

