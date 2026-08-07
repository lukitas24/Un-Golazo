const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Notifica al usuario cuando el estado de su reserva cambia.
 * Se dispara cuando un documento en la colección 'reservas' se actualiza.
 */
exports.onReservaUpdated = functions.firestore
    .document('reservas/{reservaId}')
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();

        // Solo actuar si el estado cambió
        if (newData.estado === oldData.estado) return null;

        const uid = newData.usuarioId;
        if (!uid) {
            console.log('No hay usuarioId en la reserva:', context.params.reservaId);
            return null;
        }

        let titulo = 'Actualización de reserva';
        let mensaje = `Tu reserva en ${newData.canchaNombre} ha cambiado a: ${newData.estado}`;

        // Personalizar según el nuevo estado
        if (newData.estado === 'Confirmada') {
            titulo = '¡Reserva Confirmada! ⚽';
            mensaje = `¡Buenas noticias! Tu reserva en ${newData.canchaNombre} para el ${newData.fecha} ha sido aprobada.`;
        } else if (newData.estado === 'Rechazada') {
            titulo = 'Reserva rechazada ❌';
            mensaje = `Lo sentimos, tu reserva en ${newData.canchaNombre} no pudo ser confirmada.`;
        }

        try {
            // 1. Obtener los tokens de los dispositivos del usuario
            const dispositivosSnapshot = await admin.firestore()
                .collection('jugadores')
                .document(uid)
                .collection('dispositivos')
                .where('activo', '==', true)
                .get();

            if (dispositivosSnapshot.empty) {
                console.log('El usuario no tiene dispositivos registrados:', uid);
                return null;
            }

            const tokens = dispositivosSnapshot.docs.map(doc => doc.data().token);

            // 2. Definir la carga útil de la notificación
            const payload = {
                notification: {
                    title: titulo,
                    body: mensaje,
                    clickAction: 'FLUTTER_NOTIFICATION_CLICK', // O el equivalente en Android si usas intents específicos
                },
                data: {
                    tipo: 'RESERVA_STATUS_CHANGE',
                    reservaId: context.params.reservaId,
                    estado: newData.estado,
                    canchaId: newData.canchaId || '',
                    partidoId: newData.partidoId || ''
                }
            };

            // 3. Enviar a través de FCM (Firebase Cloud Messaging)
            const response = await admin.messaging().sendEachForMulticast({
                tokens: tokens,
                notification: payload.notification,
                data: payload.data,
                android: {
                    priority: 'high',
                    notification: {
                        channelId: 'geofence_channel' // Usamos el canal ya creado en la app
                    }
                }
            });

            console.log(`Notificaciones enviadas con éxito para reserva ${context.params.reservaId}. Éxitos: ${response.successCount}`);

            // Limpieza opcional: si un token falla porque ya no es válido, podrías desactivarlo aquí.
            return null;
        } catch (error) {
            console.error('Error enviando notificación:', error);
            return null;
        }
    });

/**
 * Notifica a los participantes de un partido cuando se publica/actualiza.
 * (Opcional: implementado como ejemplo adicional)
 */
exports.onPartidoCreated = functions.firestore
    .document('partidos/{partidoId}')
    .onCreate(async (snapshot, context) => {
        const partido = snapshot.data();

        // Aquí podrías enviar una notificación a todos los usuarios de una zona cercana
        // o a los amigos del creador.
        console.log('Nuevo partido creado:', partido.titulo);
        return null;
    });
