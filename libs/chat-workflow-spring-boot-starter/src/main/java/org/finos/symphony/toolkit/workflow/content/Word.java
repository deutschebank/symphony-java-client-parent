package org.finos.symphony.toolkit.workflow.content;

public interface Word extends Content {

	public String getIdentifier();
	
	public static Word of(String s) {
		return new Word() {

			@Override
			public String getIdentifier() {
				return s.replaceAll("[^a-zA-Z0-9]", "");
			}

			@Override
			public String getText() {
				return s;
			}

			@Override
			public int hashCode() {
				return getIdentifier().hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof Word) {
					return this.getIdentifier().equals(((Word) obj).getIdentifier());
				} else {
					return false;
				}
			}

			@Override
			public String toString() {
				return "Word ["+s+"]";
			}
			
			
		};
	}
}
