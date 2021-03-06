/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Member;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MemberSetPatcher<T extends Member> extends AnnotatableSetPatcher<T> {

	private final Action defaultAction;
	protected final Action staticConstructorAction;
	protected final Action resolvedStaticConstructorAction;

	public MemberSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent);
		defaultAction = annotation.getDefaultAction();
		staticConstructorAction = annotation.getStaticConstructorAction();
		resolvedStaticConstructorAction = (staticConstructorAction != null ? staticConstructorAction : defaultAction);
	}

	// Implementation

	@Override
	protected Action getDefaultAction(String patchId, T patch) {
		if (defaultAction != null) {
			log(INFO, "default " + defaultAction.getLabel());
			return defaultAction;
		} else {
			log(ERROR, "no default action defined");
			return Action.IGNORE;
		}
	}

	@Override
	protected void onPrepare(String patchId, T patch, PatcherAnnotation annotation) throws PatchException {
		if (annotation.getTargetClass() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_TARGET_CLASS);
		if (annotation.getStaticConstructorAction() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_STATIC_CONSTRUCTOR_ACTION);
		if (annotation.getDefaultAction() != null) PatcherAnnotation.throwInvalidElement(Marker.ELEM_DEFAULT_ACTION);
		if (annotation.getOnlyEditMembers()) PatcherAnnotation.throwInvalidElement(Marker.ELEM_ONLY_EDIT_MEMBERS);
		if (annotation.getRecursive()) PatcherAnnotation.throwInvalidElement(Marker.ELEM_RECURSIVE);
	}

	@Override
	protected T onSimpleEdit(T patch, PatcherAnnotation annotation, T target, boolean inPlaceEdit) {
		int flags1 = patch.getAccessFlags();
		int flags2 = target.getAccessFlags();
		// Avoid duplicated messages if not renaming.
		if (!inPlaceEdit) {
			String message = "'%s' modifier mismatch in targeted and edited members";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, BRIDGE, SYNTHETIC, CONSTRUCTOR }, message);
		} else {
			String message = "'%s' modifier mismatch in original and edited versions";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC, VARARGS, NATIVE, ABSTRACT, ENUM, CONSTRUCTOR, DECLARED_SYNCHRONIZED }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, STRICTFP }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { BRIDGE, SYNTHETIC }, message);
		}
		return patch;
	}

	@Override
	protected void onEffectiveReplacement(String id, T patch, T patched, T original, boolean inPlaceEdit) {
		// Avoid duplicated messages if not renaming.
		if (!inPlaceEdit) {
			int flags1 = patched.getAccessFlags();
			int flags2 = original.getAccessFlags();
			String message = "'%s' modifier mismatch in original and replacement members";
			if (isLogging(WARN)) checkAccessFlags(WARN, flags1, flags2,
					new AccessFlags[] { STATIC,  ABSTRACT, ENUM, CONSTRUCTOR }, message);
			if (isLogging(INFO)) checkAccessFlags(INFO, flags1, flags2,
					new AccessFlags[] { PUBLIC, PRIVATE, PROTECTED, FINAL, VOLATILE, TRANSIENT, VARARGS }, message);
			if (isLogging(DEBUG)) checkAccessFlags(DEBUG, flags1, flags2,
					new AccessFlags[] { SYNCHRONIZED, BRIDGE, NATIVE, STRICTFP, SYNTHETIC, DECLARED_SYNCHRONIZED }, message);
		}
	}

}
