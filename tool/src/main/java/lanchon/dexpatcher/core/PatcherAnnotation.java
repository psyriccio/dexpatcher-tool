/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.BooleanEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.EnumEncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.iface.value.TypeEncodedValue;

public class PatcherAnnotation {

	public static PatcherAnnotation parse(Context context, Set<? extends Annotation> annotations) throws PatchException {

		Annotation annotation = null;
		Action action = null;
		Set<Annotation> filteredAnnotations = new LinkedHashSet<>(annotations.size());
		for (Annotation an : annotations) {
			Action ac = context.getActionFromTypeDescriptor(an.getType());
			if (ac != null) {
				if (action != null) {
					throw new PatchException("conflicting patcher annotations (" +
							action.getMarker().getClassName() + ", " + ac.getMarker().getClassName() + ")");
				}
				action = ac;
				annotation = an;
			} else {
				filteredAnnotations.add(an);
			}
		}

		if (action == null) return null;

		String target = null;
		String targetClass = null;
		Action staticConstructorAction = null;
		Action defaultAction = null;
		boolean onlyEditMembers = false;
		boolean recursive = false;
		for (AnnotationElement element : annotation.getElements()) {
			String name = element.getName();
			EncodedValue value = element.getValue();
			switch (name) {
			case Marker.ELEM_TARGET: {
				if (target != null) break;
				String s = ((StringEncodedValue) value).getValue();
				if (s.length() != 0) target = s;
				continue;
			}
			case Marker.ELEM_TARGET_CLASS: {
				if (targetClass != null) break;
				String s = ((TypeEncodedValue) value).getValue();
				if (!Marker.TYPE_VOID.equals(s)) targetClass = s;
				continue;
			}
			case Marker.ELEM_STATIC_CONSTRUCTOR_ACTION: {
				if (staticConstructorAction != null) break;
				String s = ((EnumEncodedValue) value).getValue().getName();
				staticConstructorAction = Action.valueOf(s);
				continue;
			}
			case Marker.ELEM_DEFAULT_ACTION: {
				if (defaultAction != null) break;
				String s = ((EnumEncodedValue) value).getValue().getName();
				defaultAction = Action.valueOf(s);
				continue;
			}
			case Marker.ELEM_ONLY_EDIT_MEMBERS: {
				if (onlyEditMembers) break;
				onlyEditMembers = ((BooleanEncodedValue) value).getValue();
				continue;
			}
			case Marker.ELEM_RECURSIVE: {
				if (recursive) break;
				recursive = ((BooleanEncodedValue) value).getValue();
				continue;
			}
			default:
				break;
			}
			throwInvalidElement(name);
		}

		if (target != null && targetClass != null) {
			throw new PatchException("conflicting patcher annotation elements (" +
					Marker.ELEM_TARGET + ", " + Marker.ELEM_TARGET_CLASS + ")");
		}

		return new PatcherAnnotation(action, target, targetClass, staticConstructorAction, defaultAction,
				onlyEditMembers, recursive, Collections.unmodifiableSet(filteredAnnotations));

	}

	public static PatchException throwInvalidAnnotation(Marker marker) throws PatchException {
		throw new PatchException("invalid patcher annotation (" + marker.getClassName() + ")");
	}

	public static PatchException throwInvalidElement(String name) throws PatchException {
		throw new PatchException("invalid patcher annotation element (" + name + ")");
	}

	private final Action action;
	private final String target;
	private final String targetClass;
	private final Action staticConstructorAction;
	private final Action defaultAction;
	private final boolean onlyEditMembers;
	private final boolean recursive;
	private final Set<? extends Annotation> filteredAnnotations;

	public PatcherAnnotation(Action action, Set<? extends Annotation> filteredAnnotations) {
		this(action, null, null, null, null, false, false, filteredAnnotations);
	}

	public PatcherAnnotation(Action action, String target, String targetClass,
			Action staticConstructorAction, Action defaultAction, boolean onlyEditMembers,
			boolean recursive, Set<? extends Annotation> filteredAnnotations) {
		if (action == null) throw new AssertionError("Null action");
		this.action = action;
		this.target = target;
		this.targetClass = targetClass;
		this.staticConstructorAction = staticConstructorAction;
		this.defaultAction = defaultAction;
		this.onlyEditMembers = onlyEditMembers;
		this.recursive = recursive;
		this.filteredAnnotations = filteredAnnotations;
	}

	public Action getAction() {
		return action;
	}

	public String getTarget() {
		return target;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public Action getStaticConstructorAction() {
		return staticConstructorAction;
	}

	public Action getDefaultAction() {
		return defaultAction;
	}

	public boolean getOnlyEditMembers() {
		return onlyEditMembers;
	}

	public boolean getRecursive() {
		return recursive;
	}

	public Set<? extends Annotation> getFilteredAnnotations() {
		return filteredAnnotations;
	}

}
