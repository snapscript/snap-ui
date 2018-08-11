package org.snapscript.ui;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;

@Data
@AllArgsConstructor
public class WindowIcon {

   private final URL resource;
   private final byte[] data;
}
